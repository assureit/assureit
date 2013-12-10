/// <reference path="../../src/CaseModel.ts" />
/// <reference path="../../src/PlugInManager.ts" />

class DScriptGenerator {
	Indent: string = "\t";
 	LineFeed: string = "\n";
 	LibraryManager: DScriptLibraryManager = new DScriptLibraryManager();

	VisitGoalNode(node: AssureIt.NodeModel): string {
		return "";
	}
	VisitStrategyNode(node: AssureIt.NodeModel): string {
		return "";
	}
	VisitEvidenceNode(node: AssureIt.NodeModel): string {
		return "";
	}
	VisitContextNode(node: AssureIt.NodeModel): string {
		return "";
	}
	GeneratePreface(): string {
		return "";
	}
	GenerateMainFunction(dscriptActionMap: DScriptActionMap): string {
		return "";
	}
};

class DShellCodeGenerator extends DScriptGenerator {
	VisitGoalOrStrategyNode(node: AssureIt.NodeModel): string {
		var ret: string = "";
		ret += "DFault " + node.Label + "() {" + this.LineFeed;
		ret += this.Indent + "return ";
		var children = node.Children;
		if (children.length > 0) {
			for (var i: number = 0; i < children.length; i++) {
				var child = children[i];
				ret += child.Label + "()";
				break;//FIX ME!!
			}
		}
		else {
			ret += "null/*undevelopped*/";
		}
		ret += ";" + this.LineFeed;
		ret += "}" + this.LineFeed;
		return ret;
	}
	GenerateLocalVariable(node: AssureIt.NodeModel): string {
		var ret: string = "";
		var env = node.Environment;
		for (var key in env) {
			if (key == "prototype" || key == "Reaction") {
				continue;
			}
			else {
				ret += this.Indent + "let " + key + " = \"" + env[key] + "\";" + this.LineFeed;
			}
		}
		return ret;
	}
	GenerateAction(node: AssureIt.NodeModel): string {
		var ret: string = "";

 		/* Define Action Function */
		ret += this.GenerateLocalVariable(node);
		var funcName: string = node.GetNote("Action"); //already check whether funcName is Null
		var actionFunctionDef: string = this.LibraryManager.GetLibraryFunction(funcName.replace("()", ""));
		var monitor_raw: string = node.Environment["Monitor"];
		if (monitor_raw != null) {
			var monitor_fixed: string = monitor_raw
				.replace(/\{|\}/g, "")
				.replace(/[a-zA-Z]+/g, function(matchedStr) {
					return "GetDataFromRec(Location, \"" + matchedStr + "\")";
				}).trim();
			actionFunctionDef = actionFunctionDef
				.replace(/[\(\w]Monitor[\)\w]/g, function(matchedStr) {
					return matchedStr.replace("Monitor", monitor_fixed);
				});
		}
		ret += this.Indent + actionFunctionDef.replace(/\n/g, "\n\t") + this.LineFeed;

		/* Call Action Function */
		ret += this.Indent + "DFault ret = null;" + this.LineFeed;
		ret += this.Indent + "if(Location == LOCATION) {" + this.LineFeed;
		ret += this.Indent + this.Indent + "ret = dlog " + funcName + ";" + this.LineFeed;
		ret += this.Indent + "}" + this.LineFeed;
		ret += this.Indent + "return ret;" + this.LineFeed;

		return ret;
	}

	VisitGoalNode(node: AssureIt.NodeModel): string {
		return this.VisitGoalOrStrategyNode(node);
	}
	VisitStrategyNode(node: AssureIt.NodeModel): string {
		return this.VisitGoalOrStrategyNode(node);
	}
	VisitEvidenceNode(node: AssureIt.NodeModel): string {
		var ret: string = "";
		ret += "DFault " + node.Label + "() {" + this.LineFeed;
		if (node.GetNote("Action") != null) {
			ret += this.GenerateAction(node);
		}
		else {
			ret += this.Indent + "return null/*static evidence*/;";
		}
		ret += "}" + this.LineFeed;
		return ret;
	}
	VisitContextNode(node:AssureIt.NodeModel): string {
		var ret: string = "";
		ret += "DFault " + node.Label + "() {" + this.LineFeed;
		ret += this.Indent + "return null;" + this.LineFeed;
		ret += "}" + this.LineFeed;
		return ret;
	}

	GenerateMainFunction(dscriptActionMap: DScriptActionMap): string { //TODO
		var ret: string = "";
		return ret;
	}
}

class ErlangCodeGenerator extends DScriptGenerator {
	GenNodeFuncName(node: AssureIt.NodeModel): string {
		return node.Label.toLowerCase();
	}
	VisitGoalOrStrategyNode(node: AssureIt.NodeModel): string {
		var ret: string = "";
		ret += this.GenNodeFuncName(node) + "() ->" + this.LineFeed;
		ret += this.Indent;
		var children = node.Children;
		if (children.length > 0) {
			ret += "lists:foldl(fun add/2, null, [";
			for (var i: number = 0; i < children.length; i++) {
				var child = children[i];
				ret += this.GenNodeFuncName(child) + "()";
				if (i < children.length - 1) ret += ", ";
			}
			ret += "]).";
		}
		else {
			ret += "null.%%undevelopped";
		}
		ret += this.LineFeed;
		return ret;
	}
	GenerateLocalVariable(node: AssureIt.NodeModel): string {
		var ret: string = "";
		var env = node.Environment;
		for (var key in env) {
			if (key == "prototype" || key == "Reaction") {
				continue;
			}
			else {
				ret += this.Indent + "let " + key + " = \"" + env[key] + "\";" + this.LineFeed;
			}
		}
		return ret;
	}
	GenerateAction(node: AssureIt.NodeModel): string {
		var ret: string = "";

		/* Define Action Function */
		ret += this.GenerateLocalVariable(node);
		var funcName = node.GetNote("Action"); //already check whether funcName is Null
		var actionFunctionDef = this.LibraryManager.GetLibraryFunction(funcName.replace("()", ""));
		var monitor_raw = node.Environment["Monitor"];
		if (monitor_raw != null) {
			var monitor_fixed: string = monitor_raw
				.replace(/\{|\}/g, "")
				.replace(/[a-zA-Z]+/g, function(matchedStr) {
					return "GetDataFromRec(Location, \"" + matchedStr + "\")";
				}).trim();
			actionFunctionDef = actionFunctionDef
				.replace(/[\(\w]Monitor[\)\w]/g, function(matchedStr) {
					return matchedStr.replace("Monitor", monitor_fixed);
				});
		}
		ret += this.Indent + actionFunctionDef.replace(/\n/g, "\n\t") + this.LineFeed;

		/* Call Action Function */
		ret += this.Indent + "DFault ret = null;" + this.LineFeed;
		ret += this.Indent + "if(Location == LOCATION) {" + this.LineFeed;
		ret += this.Indent + this.Indent + "ret = dlog " + funcName + ";" + this.LineFeed;
		ret += this.Indent + "}" + this.LineFeed;
		ret += this.Indent + "return ret;" + this.LineFeed;

		return ret;

	}

	VisitGoalNode(node: AssureIt.NodeModel): string {
		return this.VisitGoalOrStrategyNode(node);
	}
	VisitStrategyNode(node: AssureIt.NodeModel): string {
		return this.VisitGoalOrStrategyNode(node);
	}
	VisitEvidenceNode(node: AssureIt.NodeModel): string {
		var ret: string = "";
		ret += this.GenNodeFuncName(node) + "() ->" + this.LineFeed;
		if (node.GetNote("Action") != null) {
			//FIX ME!!
			//ret += this.GenerateAction(node);
			ret += this.Indent + "null.%%static evidence";
		}
		else {
			ret += this.Indent + "null.%%static evidence";
		}
		ret += this.LineFeed;
		return ret;
	}
	VisitContextNode(node: AssureIt.NodeModel): string {
		var ret: string = "";
		ret += this.GenNodeFuncName(node) + "() ->" + this.LineFeed;
		ret += this.Indent + "null." + this.LineFeed;
		return ret;
	}
	GeneratePreface(): string {
		var ret: string = "";
		ret += "-module(dscript)." + this.LineFeed;
		ret += "-export([main/0])." + this.LineFeed;
		ret += "main() -> null." + this.LineFeed;
		ret += "add(null, null) -> null;" + this.LineFeed;
		ret += "add(null, Ret) -> Ret;" + this.LineFeed;
		ret += "add(X, null) -> X;" + this.LineFeed;
		ret += "add(_X, _Ret) -> dfault." + this.LineFeed;
		return ret;
	}
	GenerateMainFunction(dscriptActionMap: DScriptActionMap): string {
		return "";
	}
};

class DScriptLibraryManager {
	ServerApi: AssureIt.ServerAPI;
	Cache: { [funcName: string]: string };

	constructor() {
		this.ServerApi = null;
		this.Cache = {};
	}
	GetLibraryFunction(funcName: string): string {
		var ret = "DFault ${funcName}() { return null; }".replace("${funcName}", funcName);
		if (this.ServerApi == null) {
			console.log("DScriptLibraryManager error : not set ServerApi yet");
		}
		else if (funcName in this.Cache) {
			ret = this.Cache[funcName];
		}
		else {
			var script = this.ServerApi.GetDScript(funcName);
			if (script != null) {
				ret = script.script;
				this.Cache[funcName] = script.script;
			}
		}
		return ret;
	}
}
