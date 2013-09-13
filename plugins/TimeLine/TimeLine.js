var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
var TimeLine = (function () {
    function TimeLine(caseViewer, nodeModel, element, serverApi) {
        this.caseViewer = caseViewer;
        this.nodeModel = nodeModel;
        this.element = element;
        this.serverApi = serverApi;
    }
    TimeLine.prototype.CreateDOM = function () {
        this.root = $(this.caseViewer.Screen.ControlLayer);

        var node = $("#" + this.nodeModel.Label);

        this.container = $("<div></div>").css({
            position: "absolute",
            left: node.position().left + (node.width() / 2),
            top: node.position().top + node.height() + 53
        }).addClass("timeline-container").appendTo(this.root);
        this.timeline = $("<div></div>").addClass("timeline").appendTo(this.container);
    };

    TimeLine.prototype.Enable = function (callback) {
        var _this = this;
        this.CreateDOM();

        var commits = this.serverApi.GetCommitList(this.nodeModel.Case.CaseId);
        var Case = this.nodeModel.Case;
        var TopLabel = Case.ElementTop.Label;
        var decoder = new AssureIt.CaseDecoder();
        this.timeline.append($('<ul id="timeline-ul"></ul>'));
        commits.forEach(function (i, v) {
            $("#timeline-ul").append($('<a id="timeline' + i + '" href="#"></a>').text(v.toString()));
            $("#timeline" + i).click(function (e) {
                var loc = _this.serverApi.basepath + "case/" + _this.nodeModel.Case.CaseId;
                location.href = loc + '/history/' + (i);
            });
        });
    };

    TimeLine.prototype.Disable = function (callback) {
        $(".timeline-container").remove();
        callback();
    };
    return TimeLine;
})();

var TimeLinePlugIn = (function (_super) {
    __extends(TimeLinePlugIn, _super);
    function TimeLinePlugIn(plugInManager) {
        _super.call(this, plugInManager);
        this.MenuBarContentsPlugIn = new TimeLineMenuPlugIn(plugInManager);
        this.ShortcutKeyPlugIn = new TimeLineKeyPlugIn(plugInManager);
    }
    return TimeLinePlugIn;
})(AssureIt.PlugInSet);

var TimeLineMenuPlugIn = (function (_super) {
    __extends(TimeLineMenuPlugIn, _super);
    function TimeLineMenuPlugIn(plugInManager) {
        _super.call(this, plugInManager);
        this.visible = true;
    }
    TimeLineMenuPlugIn.prototype.IsEnabled = function (caseViewer, caseModel) {
        return true;
    };

    TimeLineMenuPlugIn.prototype.Delegate = function (caseViewer, caseModel, element, serverApi) {
        var loc = serverApi.basepath + "case/" + caseModel.Case.CaseId + "/history";
        element.append('<a href="' + loc + '" ><img id="timeline" src="' + serverApi.basepath + 'images/icon.png" title="History" alt="history" /></a>');

        return true;
    };
    return TimeLineMenuPlugIn;
})(AssureIt.MenuBarContentsPlugIn);

var TimeLineKeyPlugIn = (function (_super) {
    __extends(TimeLineKeyPlugIn, _super);
    function TimeLineKeyPlugIn(plugInManager) {
        _super.call(this, plugInManager);
        this.plugInManager = plugInManager;
    }
    TimeLineKeyPlugIn.prototype.IsEnabled = function (Case0, serverApi) {
        return true;
    };

    TimeLineKeyPlugIn.prototype.RegisterKeyEvents = function (Case0, serverApi) {
        var _this = this;
        $("body").keydown(function (e) {
            if (e.keyCode == 37 && e.shiftKey) {
                _this.ShowPreview(Case0, serverApi);
            }
            if (e.keyCode == 39 && e.shiftKey) {
                _this.ShowNext(Case0, serverApi);
            }
        });
        return true;
    };

    TimeLineKeyPlugIn.prototype.GetHistoryId = function () {
        var url = location.href;
        var matches = url.match(/history\/([0-9]*)/);
        if (matches != null) {
            return Number(matches[1]);
        }
        return -1;
    };

    TimeLineKeyPlugIn.prototype.ShowPreview = function (Case, serverApi) {
        var historyId = this.GetHistoryId();
        if (historyId == -1) {
        }
        if (historyId > 0) {
            historyId--;
            var loc = serverApi.basepath + "case/" + Case.CaseId;
            location.href = loc + '/history/' + (historyId);
        }
    };

    TimeLineKeyPlugIn.prototype.ShowNext = function (Case, serverApi) {
        var historyId = this.GetHistoryId();
        if (historyId == -1) {
        }
        if (historyId < 5) {
            historyId++;
            var loc = serverApi.basepath + "case/" + Case.CaseId;
            location.href = loc + '/history/' + (historyId);
        }
    };

    TimeLineKeyPlugIn.prototype.DeleteFromDOM = function () {
    };

    TimeLineKeyPlugIn.prototype.DisableEvent = function (caseViewer, case0, serverApi) {
    };
    return TimeLineKeyPlugIn;
})(AssureIt.ShortcutKeyPlugIn);
