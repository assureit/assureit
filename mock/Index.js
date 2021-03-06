$(function () {
    var serverApi = new AssureIt.ServerAPI('', 'http://54.250.206.119/rec/api/2.0/', 'http://localhost:8081');
    var pluginManager = new AssureIt.PlugInManager('');
    pluginManager.SetPlugIn("menu", new MenuBarPlugIn(pluginManager));
    pluginManager.SetPlugIn("scale", new ScalePlugIn(pluginManager));
    pluginManager.SetPlugIn("editor", new EditorPlugIn(pluginManager));
    pluginManager.SetPlugIn("simplepattern", new SimplePatternPlugIn(pluginManager));
    pluginManager.SetPlugIn("dscript", new DScriptPlugIn(pluginManager));
    pluginManager.SetPlugIn("fullscreeneditor", new FullScreenEditorPlugIn(pluginManager));
    pluginManager.SetPlugIn("hover", new HoverPlugIn(pluginManager));
    pluginManager.SetPlugIn("statements", new DefaultStatementRenderPlugIn(pluginManager));
    pluginManager.SetPlugIn("annotation", new AnnotationPlugIn(pluginManager));
    pluginManager.SetPlugIn("note", new NotePlugIn(pluginManager));
    pluginManager.SetPlugIn("monitor", new MonitorPlugIn(pluginManager));
    pluginManager.SetPlugIn("export", new ExportPlugIn(pluginManager));
    pluginManager.SetPlugIn("portraitlayout", new LayoutPortraitPlugIn(pluginManager));
    pluginManager.SetPlugIn("search", new SearchNodePlugIn(pluginManager));
    pluginManager.SetUseLayoutEngine("portraitlayout");

    var JsonData = {
        "DCaseName": "test",
        "NodeCount": 25,
        "TopGoalLabel": "G1",
        "NodeList": "*G1\n*C1 @Def\n*S1\n**G2\nFuga\n**S2\nHoge\n**C3 @Def\nHoge\n***G4\nHoge\nFuga\n***E1\nFuga\n***G5\nHoge\n***C2 @Def\nFuga\n***E2\nFuga\n***C4 @Def\nHoge\n***E3\nFuga\n***G6\nFuga\n***E4\n**G3\n**S3\n***G7\n***C5\n***E5\n***E6\n***G8\n***C6\n***E7\n***G9"
    };

    var Case0 = new AssureIt.Case(JsonData.DCaseName, '', JsonData.NodeList, 1, 0, pluginManager);
    Case0.SetEditable(true);
    var caseDecoder = new AssureIt.CaseDecoder();
    var root = caseDecoder.ParseASN(Case0, JsonData.NodeList, null);
    console.log(root);

    Case0.SetElementTop(root);

    var backgroundlayer = document.getElementById("background");
    var shapelayer = document.getElementById("layer0");
    var contentlayer = document.getElementById("layer1");
    var controllayer = document.getElementById("layer2");

    var Screen = new AssureIt.ScreenManager(shapelayer, contentlayer, controllayer, backgroundlayer);

    var Viewer = new AssureIt.CaseViewer(Case0, pluginManager, serverApi, Screen);
    pluginManager.RegisterKeyEvents(Viewer, Case0, serverApi);
    Viewer.Draw();
});
