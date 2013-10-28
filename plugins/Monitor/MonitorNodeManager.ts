/// <reference path="../DScript/ActionNodeManager.ts" />


class MonitorNodeManager extends ActionNodeManager {

	SetMonitorNode(evidenceNode: AssureIt.NodeModel) {
		var location: string = getContextNode(evidenceNode.Parent).Notes["Location"];
		var condition: string = getContextNode(evidenceNode.Parent).Notes["Monitor"];
		var item: string = extractItemFromCondition(condition);
		var monitorNode: MonitorNode = <MonitorNode>this.ActionNodeMap[evidenceNode.Label];

		if(monitorNode == null) {
			this.ActionNodeMap[evidenceNode.Label] = new MonitorNode(location, item, condition, evidenceNode);
		}
		else {
			monitorNode.SetLocation(location);
			monitorNode.SetItem(item);
			monitorNode.SetCondition(condition);
			return;
		}

		if(Object.keys(this.ActionNodeMap).length == 1) {   // manager has one monitor
			this.StartMonitoring(5000);
		}
	}

}