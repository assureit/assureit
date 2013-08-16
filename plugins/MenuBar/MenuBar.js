var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
var MenuBar = (function () {
    function MenuBar(caseViewer, model, case0, node, serverApi, plugIn) {
        this.plugIn = plugIn;
        this.caseViewer = caseViewer;
        this.model = model;
        this.case0 = case0;
        this.node = node;
        this.serverApi = serverApi;
        this.Init();
    }
    MenuBar.prototype.Init = function () {
        var self = this;

        var thisNodeType = self.model.Type;

        $('#menu').remove();
        self.menu = $('<div id="menu">' + '<a href="#" ><img id="commit" src="' + this.serverApi.basepath + 'images/commit.png" title="Commit" alt="commit" /></a>' + '<a href="#" ><img id="remove" src="' + this.serverApi.basepath + 'images/remove.png" title="Remove" alt="remove" /></a>' + '<a href="#" ><img id="scale"  src="' + this.serverApi.basepath + 'images/scale.png" title="Scale" alt="scale" /></a>' + '</div>');

        var hasContext = false;

        for (var i = 0; i < self.model.Children.length; i++) {
            if (self.model.Children[i].Type == AssureIt.NodeType.Context) {
                hasContext = true;
            }
        }
        switch (thisNodeType) {
            case AssureIt.NodeType.Goal:
                if (!hasContext) {
                    self.menu.append('<a href="#" ><img id="context"  src="' + this.serverApi.basepath + 'images/context.png" title="Context" alt="context" /></a>');
                }
                self.menu.append('<a href="#" ><img id="strategy" src="' + this.serverApi.basepath + 'images/strategy.png" title="Strategy" alt="strategy" /></a>');
                self.menu.append('<a href="#" ><img id="evidence" src="' + this.serverApi.basepath + 'images/evidence.png" title="Evidence" alt="evidence" /></a>');
                break;
            case AssureIt.NodeType.Strategy:
                self.menu.append('<a href="#" ><img id="goal"     src="' + this.serverApi.basepath + 'images/goal.png" title="Goal" alt="goal" /></a>');
                if (!hasContext) {
                    self.menu.append('<a href="#" ><img id="context"  src="' + this.serverApi.basepath + 'images/context.png" title="Context" alt="context" /></a>');
                }
                break;
            case AssureIt.NodeType.Evidence:
                if (!hasContext) {
                    self.menu.append('<a href="#" ><img id="context"  src="' + this.serverApi.basepath + 'images/context.png" title="Context" alt="context" /></a>');
                }
                break;
            default:
                break;
        }
    };

    MenuBar.prototype.AddNode = function (nodeType) {
        var thisNodeView = this.caseViewer.ViewMap[this.node.children("h4").text()];
        var newNodeModel = new AssureIt.NodeModel(this.case0, thisNodeView.Source, nodeType, null, null);
        this.case0.SaveIdCounterMax(this.case0.ElementTop);
        this.caseViewer.ViewMap[newNodeModel.Label] = new AssureIt.NodeView(this.caseViewer, newNodeModel);
        this.caseViewer.ViewMap[newNodeModel.Label].ParentShape = this.caseViewer.ViewMap[newNodeModel.Parent.Label];
        this.caseViewer.Resize();
        this.caseViewer.ReDraw();
    };

    MenuBar.prototype.GetDescendantLabels = function (labels, children) {
        for (var i = 0; i < children.length; i++) {
            labels.push(children[i].Label);
            this.GetDescendantLabels(labels, children[i].Children);
        }
        return labels;
    };

    MenuBar.prototype.RemoveNode = function () {
        var thisLabel = this.node.children("h4").text();
        var thisNodeView = this.caseViewer.ViewMap[thisLabel];
        var thisNodeModel = thisNodeView.Source;
        var brotherNodeModels = thisNodeModel.Parent.Children;
        var parentLabel = thisNodeModel.Parent.Label;
        var parentOffSet = $("#" + parentLabel).offset();

        for (var i = 0; i < brotherNodeModels.length; i++) {
            if (brotherNodeModels[i].Label == thisLabel) {
                brotherNodeModels.splice(i, 1);
            }
        }

        var labels = [thisLabel];
        labels = this.GetDescendantLabels(labels, thisNodeModel.Children);

        for (var i = 0; i < labels.length; i++) {
            delete this.case0.ElementMap[labels[i]];
            var nodeView = this.caseViewer.ViewMap[labels[i]];
            nodeView.DeleteHTMLElementRecursive(null, null);
            delete this.caseViewer.ViewMap[labels[i]];
        }

        this.caseViewer.Resize();
        this.caseViewer.ReDraw();
        var CurrentParentView = this.caseViewer.ViewMap[parentLabel];
        this.caseViewer.Screen.SetOffset(CurrentParentView.AbsX + parentOffSet.left, CurrentParentView.AbsY + parentOffSet.top);
    };

    MenuBar.prototype.Commit = function () {
        ($('#modal')).dialog('open');
    };

    MenuBar.prototype.Scale = function () {
        var self = this;

        this.caseViewer.Screen.SetScale(0.1);
        $("#background").unbind("click");
        $("#background").click(function () {
            self.caseViewer.Screen.SetScale(1);
        });
    };

    MenuBar.prototype.SetEventHandlers = function () {
        var _this = this;
        $('#goal').click(function () {
            _this.AddNode(AssureIt.NodeType.Goal);
        });

        $('#context').click(function () {
            _this.AddNode(AssureIt.NodeType.Context);
        });

        $('#strategy').click(function () {
            _this.AddNode(AssureIt.NodeType.Strategy);
        });

        $('#evidence').click(function () {
            _this.AddNode(AssureIt.NodeType.Evidence);
        });

        $('#remove').click(function () {
            _this.RemoveNode();
        });

        $('#commit').click(function () {
            _this.Commit();
        });

        $('#scale').click(function () {
            _this.Scale();
        });
    };
    return MenuBar;
})();

var CommitWindow = (function () {
    function CommitWindow() {
        this.defaultMessage = "Type your commit message...";
        this.Init();
    }
    CommitWindow.prototype.Init = function () {
        $('#modal').remove();
        var modal = $('<div id="modal" title="Commit Message" />');
        (modal).dialog({
            autoOpen: false,
            modal: true,
            resizable: false,
            draggable: false,
            show: "clip",
            hide: "fade"
        });

        var messageBox = $('<p align="center"><input id="message_box" type="text" size="30" value="' + this.defaultMessage + '" /></p>');
        messageBox.css('color', 'gray');

        var commitButton = $('<p align="right"><input id="commit_button" type="button" value="commit"/></p>');
        modal.append(messageBox);
        modal.append(commitButton);
        modal.appendTo($('layer2'));
    };

    CommitWindow.prototype.SetEventHandlers = function (caseViewer, case0, serverApi) {
        var self = this;

        $('#message_box').focus(function () {
            if ($(this).val() == self.defaultMessage) {
                $(this).val("");
                $(this).css('color', 'black');
            }
        });

        $('#message_box').blur(function () {
            if ($(this).val() == "") {
                $(this).val(self.defaultMessage);
                $(this).css('color', 'gray');
            }
        });

        $('#commit_button').click(function () {
            var encoder = new AssureIt.CaseEncoderDeprecated();
            var converter = new AssureIt.Converter();
            var contents = converter.GenOldJson(encoder.ConvertToOldJson(case0));
            serverApi.Commit(contents, $(this).val, case0.CommitId);
            window.location.reload();
        });
    };
    return CommitWindow;
})();

var MenuBarPlugIn = (function (_super) {
    __extends(MenuBarPlugIn, _super);
    function MenuBarPlugIn(plugInManager) {
        _super.call(this, plugInManager);
        this.ActionPlugIn = new MenuBarActionPlugIn(plugInManager);
    }
    return MenuBarPlugIn;
})(AssureIt.PlugIn);

var MenuBarActionPlugIn = (function (_super) {
    __extends(MenuBarActionPlugIn, _super);
    function MenuBarActionPlugIn(plugInManager) {
        _super.call(this, plugInManager);
    }
    MenuBarActionPlugIn.prototype.IsEnabled = function (caseViewer, case0) {
        return true;
    };

    MenuBarActionPlugIn.prototype.Delegate = function (caseViewer, case0, serverApi) {
        var self = this;

        $('.node').unbind('mouseenter').unbind('mouseleave');
        $('.node').hover(function () {
            var node = $(this);

            var label = node.children('h4').text();

            var model = case0.ElementMap[label];
            var menuBar = new MenuBar(caseViewer, model, case0, node, serverApi, self);
            menuBar.menu.appendTo($('#layer2'));
            menuBar.menu.css({ position: 'absolute', top: node.position().top + node.height() + 5, display: 'block', opacity: 0 });
            menuBar.menu.hover(function () {
                clearTimeout(self.timeoutId);
            }, function () {
                $(menuBar.menu).remove();
            });
            self.plugInManager.UseUILayer(self);
            menuBar.SetEventHandlers();

            var commitWindow = new CommitWindow();
            commitWindow.SetEventHandlers(caseViewer, case0, serverApi);
            self.plugInManager.InvokePlugInMenuBarContents(caseViewer, model, menuBar.menu, serverApi);

            (menuBar.menu).jqDock({
                align: 'bottom',
                fadeIn: 200,
                idle: 1500,
                size: 45,
                distance: 60,
                labels: 'tc',
                duration: 500,
                fadeIn: 1000,
                source: function () {
                    return this.src.replace(/(jpg|gif)$/, 'png');
                },
                onReady: function () {
                    menuBar.menu.css({ left: node.position().left + (node.outerWidth() - menuBar.menu.width()) / 2 });
                }
            });
        }, function () {
            self.timeoutId = setTimeout(function () {
                $('#menu').remove();
            }, 10);
        });
        return true;
    };

    MenuBarActionPlugIn.prototype.DeleteFromDOM = function () {
        $('#menu').remove();
    };
    return MenuBarActionPlugIn;
})(AssureIt.ActionPlugIn);
