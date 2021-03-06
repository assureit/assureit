/// <reference path="../../src/CaseViewer.ts" />

class Scale {

	IsScaledUp: boolean;
	ScreenManager: AssureIt.ScreenManager;
	OriginalOffsetX: number;
	OriginalOffsetY: number;
	ScaleRate: number;

	constructor(ScreenManager: AssureIt.ScreenManager) {
		this.IsScaledUp = false;
		this.ScreenManager = ScreenManager;
	}

	Up(e: JQueryEventObject) {
		this.ScaleRate = this.ScreenManager.GetScaleRate();

		if(this.ScaleRate >= 1.0) {
			/* We don't have to scale the view up */
			return false;
		}

		this.OriginalOffsetX = this.ScreenManager.GetLogicalOffsetX();   // FIXME
		this.OriginalOffsetY = this.ScreenManager.GetLogicalOffsetY();   // FIXME

		this.Zoom(0, 0, 1.0, this.ScaleRate, 500);
		return true;
	}

	DownAtOriginalPlace(e: JQueryEventObject) {
		this.Zoom(this.OriginalOffsetX, this.OriginalOffsetY, this.ScaleRate, 1.0, 500);
	}

	DownAtCurrentPlace(e: JQueryEventObject) {
		var x = this.ScreenManager.CalcLogicalOffsetXFromPageX(e.pageX);
		var y = this.ScreenManager.CalcLogicalOffsetYFromPageY(e.pageY);

		this.Zoom(x, y, this.ScaleRate, 1.0, 500);
	}

	Zoom(logicalOffsetX: number, logicalOffsetY: number, initialS: number, target: number, duration: number) {
		var cycle = 1000/30;
		var cycles = duration / cycle;
		var initialX = this.ScreenManager.GetLogicalOffsetX();
		var initialY = this.ScreenManager.GetLogicalOffsetY();
		var deltaS = (target - initialS) / cycles;
		var deltaX = (logicalOffsetX - initialX) / cycles;
		var deltaY = (logicalOffsetY - initialY)  / cycles;

		var currentS = initialS;
		var currentX = initialX;
		var currentY = initialY;
		var count = 0;

		var zoom = ()=>{
			if(count < cycles){
				count += 1;
				currentS += deltaS;
				currentX += deltaX;
				currentY += deltaY;
				this.ScreenManager.SetLogicalOffset(currentX, currentY, currentS);
				setTimeout(zoom, cycle);
			}else{
				this.ScreenManager.SetScale(1);
				this.ScreenManager.SetLogicalOffset(logicalOffsetX, logicalOffsetY, target);
			}
		}
		zoom();
	}

}

class ScalePlugIn extends AssureIt.PlugInSet {

	constructor(plugInManager: AssureIt.PlugInManager) {
		super(plugInManager);
		this.ActionPlugIn = new ScaleActionPlugIn(plugInManager);
	}
}

class ScaleActionPlugIn extends AssureIt.ActionPlugIn {

	Scale: Scale;

	EscapeKeyEventHandler: (e: JQueryEventObject) => any;
	ClickEventHandler: (e: JQueryEventObject) => any;
	DoubleClickEventHandler: (e: JQueryEventObject) => any;

	constructor(plugInManager: AssureIt.PlugInManager) {
		super(plugInManager);
		this.Scale == null;
	}

	IsEnabled(caseViewer: AssureIt.CaseViewer, case0: AssureIt.Case): boolean {
		return true;
	}

	Delegate(caseViewer: AssureIt.CaseViewer, case0: AssureIt.Case, serverApi: AssureIt.ServerAPI): boolean {
		if(this.Scale == null) {
			this.Scale = new Scale(caseViewer.Screen);
			var self = this;

			this.EscapeKeyEventHandler = function(e: JQueryEventObject) {
				if(e.keyCode == 27 /* ESC */) {
					e.stopPropagation();
					if(self.Scale.IsScaledUp) {
						self.Scale.DownAtOriginalPlace(e);
						self.Scale.IsScaledUp = false;
					}
					else {
						self.Scale.IsScaledUp = self.Scale.Up(e);
					}
				}
			};

			this.ClickEventHandler = function(e: JQueryEventObject) {
			};

			this.DoubleClickEventHandler = function(e: JQueryEventObject) {
				if(self.Scale.IsScaledUp) {
					self.Scale.DownAtCurrentPlace(e);
					self.Scale.IsScaledUp = false;
				}
			};

			$('body').keydown(this.EscapeKeyEventHandler);

			$('#background').click(this.ClickEventHandler);

			$('#background').dblclick(this.DoubleClickEventHandler);
		}

		return true;
	}

	DisableEvent(caseViewer: AssureIt.CaseViewer, case0: AssureIt.Case, serverApi: AssureIt.ServerAPI): void {
		$('body').unbind('keydown', this.EscapeKeyEventHandler);
		$('#background').unbind('click', this.ClickEventHandler);
		$('#background').unbind('dblclick', this.DoubleClickEventHandler);
	}

}
