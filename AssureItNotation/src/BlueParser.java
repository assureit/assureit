import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

class StringReader {
	int CurrentPos;
	int PreviousPos;
	String Text;

	StringReader(String Text) {
		this.Text = Text;
		this.CurrentPos = 0;
		this.PreviousPos = 0;
	}

	boolean HasNext() {
		return this.CurrentPos < this.Text.length();
	}

	String ReadLine() {
		int StartPos = this.CurrentPos;
		this.PreviousPos = this.CurrentPos;
		int i;
		for (i = this.CurrentPos; i < this.Text.length(); i++) {
			char ch = this.Text.charAt(i);
			if (ch == '\n') {
				int EndPos = i;
				this.CurrentPos = i + 1;
				return this.Text.substring(StartPos, EndPos).trim();
			}
			if (ch == '\r') {
				int EndPos = i;
				if (i + 1 < this.Text.length()
						&& this.Text.charAt(i + 1) == '\n') {
					i++;
				}
				this.CurrentPos = i + 1;
				return this.Text.substring(StartPos, EndPos).trim();
			}
		}
		this.CurrentPos = i;
		if (StartPos == this.CurrentPos) {
			return null;
		}
		return this.Text.substring(StartPos, this.CurrentPos).trim();
	}

	public void LineBack() {
		this.CurrentPos = this.PreviousPos;
	}
}

class StringWriter {
	public final static String LineFeed = "\n";
	StringBuilder sb;

	StringWriter() {
		this.sb = new StringBuilder();
	}

	void print(String s) {
		this.sb.append(s);
	}

	void println(String s) {
		this.sb.append(s);
		this.sb.append(LineFeed);
	}

	void println() {
		this.sb.append(LineFeed);
	}

	public String toString() {
		return sb.toString();
	}
}

enum GSNType {
	Goal, Context, Strategy, Evidence, Undefined;
}

class History {
	int Rev;
	String Author;
	String Role;
	String Date;
	String Process;
	public GSNDoc Doc;

	History(int Rev, String Author, String Role, String Date, String Process, GSNDoc Doc) {
		this.Rev = Rev;
		this.Author = Author;
		this.Role = Role;
		this.Date = Date;
		if (Date == null) {
			SimpleDateFormat Format = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ssZ");
			this.Date = Format.format(new Date());
		}
		this.Process = Process;
		this.Doc = Doc;
	}

	public String toString() {
		return this.Author + ";" + this.Role + ";" + this.Date + ";" + this.Process;
	}

	public boolean EqualsHistory(History aHistory) {
		return (this.Date.equals(aHistory.Date) && this.Author.equals(aHistory.Author));
	}

	public int CompareDate(History aHistory) {
		return (this.Date.compareTo(aHistory.Date));
	}

}

class WikiSyntax {
	public final static String VersionDelim = "=====";

	static int ParseInt(String NumText, int DefVal) {
		try {
			return Integer.parseInt(NumText);
		} catch (Exception e) {
		}
		return DefVal;
	}

	static int ParseGoalLevel(String LabelLine) {
		int GoalLevel = 0;
		for (int i = 0; i < LabelLine.length(); i++) {
			if (LabelLine.charAt(i) != '*') break;
			GoalLevel++;
		}
		return GoalLevel;
	}

	static String FormatGoalLevel(int GoalLevel) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < GoalLevel; i++) {
			sb.append('*');
		}
		return sb.toString();
	}

	static GSNType ParseNodeType(String LabelLine) {
		int i;
		for (i = 0; i < LabelLine.length(); i++) {
			if (LabelLine.charAt(i) != '*') break;
		}
		for (; i < LabelLine.length(); i++) {
			if (LabelLine.charAt(i) != ' ') break;
		}
		if (i < LabelLine.length()) {
			char ch = LabelLine.charAt(i);
			if (ch == 'G') {
				return GSNType.Goal;
			}
			if (ch == 'C') {
				return GSNType.Context;
			}
			if (ch == 'E') {
				return GSNType.Evidence;
			}
			if (ch == 'S') {
				return GSNType.Strategy;
			}
		}
		return GSNType.Undefined;
	}

	static String FormatNodeType(GSNType NodeType) {
		switch (NodeType) {
		case Goal:
			return "G";
		case Context:
			return "C";
		case Strategy:
			return "S";
		case Evidence:
			return "E";
		case Undefined:
		}
		return "U";
	}

	static String ParseLabelNumber(String LabelLine) {
		int StartIdx = -1;
		for (int i = 0; i < LabelLine.length(); i++) {
			if (Character.isDigit(LabelLine.charAt(i))) {
				StartIdx = i;
				break;
			}
		}
		if (StartIdx != -1) {
			for (int i = StartIdx + 1; i < LabelLine.length(); i++) {
				if (Character.isWhitespace(LabelLine.charAt(i))) {
					return LabelLine.substring(StartIdx, i);
				}
			}
			return LabelLine.substring(StartIdx);
		}
		return null;
	}

	public static String ParseRevisionHistory(String LabelLine, GSNRecord Record) {
		int Loc = LabelLine.indexOf("#");
		if (Loc != -1) {
			return LabelLine.substring(Loc).trim();
		}
		return null;
	}

	public static History[] ParseHistory(String LabelLine, GSNRecord Record) {
		int Loc = LabelLine.indexOf("#");
		if (Loc != -1) {
			History[] HistoryTriple = new History[3];
			String RevText = LabelLine.substring(Loc + 1).trim();
			String RevSet[] = RevText.split(":");
			HistoryTriple[0] = Record.GetHistory(WikiSyntax.ParseInt(RevSet[0], -1)); // Created
			HistoryTriple[1] = Record.GetHistory(WikiSyntax.ParseInt(RevSet[1], -1)); // Branched
			HistoryTriple[2] = Record.GetHistory(WikiSyntax.ParseInt(RevSet[2], -1)); // LastModified
			if (HistoryTriple[0] == null || HistoryTriple[1] == null || HistoryTriple[2] == null) {
				return null;
			}
			return HistoryTriple;
		}
		return null;
	}

	public static String FormatRefKey(GSNType NodeType, String LabelNumber, String HistoryTriple) {
		return WikiSyntax.FormatNodeType(NodeType) + LabelNumber + HistoryTriple;
	}

}

class TagUtils {
	static void ParseTag(HashMap<String, String> TagMap, String Line) {
		int loc = Line.indexOf("::");
		if (loc != -1) {
			String Key = Line.substring(0, loc).toUpperCase().trim();
			String Value = Line.substring(loc + 1).trim();
			TagMap.put(Key, Value);
		}
	}

	static void ParseHistoryTag(GSNRecord Record, String Line) {
		int loc = Line.indexOf("::");
		if (loc != -1) {
			String Key = Line.substring(0, loc).trim();
			try {
				String Value = Line.substring(loc + 1).trim();
				String[] Records = Value.split(";");
				Record.AddHistory(Integer.parseInt(Key.substring(1)), Records[0], Records[1], Records[2], Records[3]);
			} catch (Exception e) { // any parse errors are ignored
			}
		}
	}

	static void FormatTag(HashMap<String, String> TagMap, StringWriter Writer) {
		if (TagMap != null) {
			for (String Key : TagMap.keySet()) {
				Writer.println(Key + ":: " + TagMap.get(Key));
			}
		}
	}

	static void FormatHistoryTag(ArrayList<History> HistoryList, StringWriter Writer) {
		for (int i = 0; i < HistoryList.size(); i++) {
			History History = HistoryList.get(i);
			if (History != null) {
				Writer.println("#" + i + "::" + History);
			}
		}
	}

	static String GetString(HashMap<String, String> TagMap, String Key, String DefValue) {
		if (TagMap != null) {
			String Value = TagMap.get(Key);
			if (Value != null) {
				return Value;
			}
		}
		return DefValue;
	}

	static int GetInteger(HashMap<String, String> TagMap, String Key, int DefValue) {
		if (TagMap != null) {
			try {
				return Integer.parseInt(TagMap.get(Key));
			} catch (Exception e) {
			}
		}
		return DefValue;
	}

}

class MD5 {
	static MessageDigest GetMD5() {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			return digest;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	static void UpdateMD5(MessageDigest md, String Text) {
		md.update(Text.getBytes());
	}

	static void FormatDigest(byte[] Digest, StringWriter Writer) {
		if (Digest != null) {
			for (int i = 0; i < Digest.length; i++) {
				int hex = Digest[i] < 0 ? 256 + Digest[i] : Digest[i];
				// Stream.append(":");
				if (hex < 16) {
					Writer.print("0");
				}
				Writer.print(Integer.toString(hex, 16));
			}
		}
	}

	static boolean EqualsDigest(byte[] Digest, byte[] Digest2) {
		if (Digest != null && Digest2 != null) {
			for (int i = 0; i < Digest.length; i++) {
				if (Digest[i] != Digest2[i])
					return false;
			}
			return true;
		}
		return Digest == null && Digest2 == null;
	}
}

class GSNNode {
	GSNDoc BaseDoc;
	GSNNode ParentNode;
	ArrayList<GSNNode> SubNodeList;
	GSNType NodeType;
	int GoalLevel; /* 1: top level */
	String LabelNumber; /* e.g, G1 G1.1 */
	int SectionCount;
	History Created;
	History Branched;
	History LastModified;
	String NodeDoc;
	boolean HasTag;
	HashMap<String, String> TagMap;
	byte[] Digest;

	GSNNode(GSNDoc BaseDoc, GSNNode ParentNode, int GoalLevel, GSNType NodeType, String LabelNumber, History[] HistoryTriple) {
		this.BaseDoc = BaseDoc;
		this.ParentNode = ParentNode;
		this.GoalLevel = GoalLevel;
		this.NodeType = NodeType;
		this.LabelNumber = LabelNumber;
		this.SectionCount = 0;
		this.SubNodeList = null;
		if (HistoryTriple != null) {
			this.Created = HistoryTriple[0];
			this.Branched = HistoryTriple[1];
			this.LastModified = HistoryTriple[2];
		} else {
			this.Created = BaseDoc.DocHistory;
			this.Branched = this.Created;
			this.LastModified = this.Created;
		}
		this.Digest = null;
		this.NodeDoc = StringWriter.LineFeed;
		this.HasTag = false;
		if (this.ParentNode != null) {
			ParentNode.AppendSubNode(this);
		}
	}

	public GSNNode Duplicate(GSNDoc BaseDoc, GSNNode ParentNode) {
		GSNNode NewNode = new GSNNode(BaseDoc, ParentNode, this.GoalLevel, this.NodeType, this.LabelNumber, null);
		NewNode.Created = this.Created;
		NewNode.Branched = this.Branched;
		NewNode.LastModified = this.LastModified;
		NewNode.Digest = this.Digest;
		NewNode.NodeDoc = this.NodeDoc;
		NewNode.HasTag = this.HasTag;
		BaseDoc.UncheckAddNode(NewNode);
		if (this.SubNodeList != null) {
			for (GSNNode Node : this.SubNodeList) {
				Node.Duplicate(BaseDoc, NewNode);
			}
		}
		return NewNode;
	}

	String GetLabel() {
		return WikiSyntax.FormatNodeType(this.NodeType) + this.LabelNumber;
	}

	String GetHistoryTriple() {
		return "#" + this.Created.Rev + ":" + this.Branched.Rev + ":"
				+ this.LastModified;
	}

	void UpdateContent(ArrayList<String> LineList) {
		int LineCount = 0;
		StringWriter Writer = new StringWriter();
		MessageDigest md = MD5.GetMD5();
		for (String Line : LineList) {
			int Loc = Line.indexOf("::");
			if (Loc > 0) {
				this.HasTag = true;
			}
			Writer.println();
			Writer.print(Line);
			if (Line.length() > 0) {
				MD5.UpdateMD5(md, Line);
				LineCount += 1;
			}
		}
		if (LineCount > 0) {
			this.Digest = md.digest();
			this.NodeDoc = Writer.toString();
		} else {
			this.Digest = null;
			this.NodeDoc = StringWriter.LineFeed;
		}
	}

	void UpdateText(String DocText) {
		ArrayList<String> LineList = new ArrayList<String>();
		StringReader Reader = new StringReader(DocText);
		while (Reader.HasNext()) {
			String Line = Reader.ReadLine();
			LineList.add(Line);
		}
		this.UpdateContent(LineList);
	}

	HashMap<String, String> GetTagMap() {
		if (this.TagMap == null && this.HasTag) {
			this.TagMap = new HashMap<String, String>();
			StringReader Reader = new StringReader(this.NodeDoc);
			while (Reader.HasNext()) {
				String Line = Reader.ReadLine();
				int Loc = Line.indexOf("::");
				if (Loc > 0) {
					TagUtils.ParseTag(this.TagMap, Line);
				}
			}
		}
		return this.TagMap;
	}

	void AppendSubNode(GSNNode Node) {
		if (this.SubNodeList == null) {
			this.SubNodeList = new ArrayList<GSNNode>();
		}
		this.SubNodeList.add(Node);
	}

	GSNNode GetCloseGoal() {
		GSNNode Node = this;
		while (Node.NodeType != GSNType.Goal) {
			Node = Node.ParentNode;
		}
		return Node;
	}

	boolean HasSubNode(GSNType NodeType) {
		if (this.SubNodeList != null) {
			for (int i = this.SubNodeList.size() - 1; i >= 0; i--) {
				GSNNode Node = this.SubNodeList.get(i);
				if (Node.NodeType == NodeType) {
					return true;
				}
			}
		}
		return false;
	}

	GSNNode GetLastNodeOrSelf() {
		if (this.SubNodeList != null) {
			return this.SubNodeList.get(this.SubNodeList.size() - 1);
		}
		return this;
	}

	GSNNode GetLastNode(GSNType NodeType) {
		if (this.SubNodeList != null) {
			for (int i = this.SubNodeList.size() - 1; i >= 0; i--) {
				GSNNode Node = this.SubNodeList.get(i);
				if (Node.NodeType == NodeType) {
					return Node;
				}
			}
		}
		if (NodeType == GSNType.Strategy) {
			return new GSNNode(this.BaseDoc, this, this.GoalLevel,
					GSNType.Strategy, this.LabelNumber, null);
		}
		return null;
	}

	void FormatNode(HashMap<String, GSNNode> RefMap, StringWriter Writer) {
		Writer.print(WikiSyntax.FormatGoalLevel(this.GoalLevel));
		Writer.print(" ");
		Writer.print(WikiSyntax.FormatNodeType(this.NodeType));
		Writer.print(this.LabelNumber);
		// Stream.append(" ");
		// MD5.FormatDigest(this.Digest, Stream);
		String RefKey = null;
		GSNNode RefNode = null;
		if (this.Created != null) {
			Writer.print(" " + this.GetHistoryTriple());
			RefKey = WikiSyntax.FormatRefKey(this.NodeType, this.LabelNumber,
					this.GetHistoryTriple());
			RefNode = RefMap.get(RefKey);
		}
		if (RefNode == null) {
			Writer.print(this.NodeDoc);
			if (this.Digest != null) {
				Writer.println();
			}
			if (RefKey != null) {
				RefMap.put(RefKey, this);
			}
		} else {
			Writer.println();
		}
		if (this.SubNodeList != null) {
			for (GSNNode Node : this.SubNodeList) {
				Node.FormatNode(RefMap, Writer);
			}
		}
	}

	// Merge
	boolean IsNewerTree(int ModifiedRev) {
		if (ModifiedRev <= this.LastModified.Rev) {
			if (this.SubNodeList != null) {
				for (GSNNode Node : this.SubNodeList) {
					if (!Node.IsNewerTree(ModifiedRev)) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	public void Merge(GSNDoc MasterDoc, int ModifiedRev, ArrayList<GSNNode> ConflictList) {
		if (ModifiedRev <= this.LastModified.Rev) {
			GSNNode MasterNode = MasterDoc.GetNode(this.GetLabel());
			if (MasterNode == null
					|| MasterNode.Created.Rev != this.Created.Rev) { // new node
			// GSNNode MasterParentNode =
			// MasterDoc.GetNode(this.ParentNode.GetLabel());
			// if(MasterParentNode != null) {
			// this.MoveWithNewLabel(MasterDoc, MasterParentNode);
			// this.ParentNode = MasterParentNode;
			// MasterParentNode.AppendSubNode(this);
			// }
			// return;
			}
			if (MasterNode.LastModified.Rev == this.Branched.Rev) {
				MasterNode.NodeDoc = this.NodeDoc;
				MasterNode.Digest = this.Digest;
				MasterNode.HasTag = this.HasTag;
				MasterNode.Branched = this.Branched;
				MasterNode.LastModified = this.LastModified;
			} else {
				ConflictList.add(this);
			}
			if (this.SubNodeList != null) {
				for (GSNNode Node : this.SubNodeList) {
					Node.Merge(MasterDoc, ModifiedRev, ConflictList);
				}
			}
		}
	}
}

class GSNDoc {
	GSNRecord Record;
	GSNNode TopGoal;
	HashMap<String, GSNNode> NodeMap;
	HashMap<String, String> DocTagMap;
	History DocHistory;
	int GoalCount;

	GSNDoc(GSNRecord Record) {
		this.Record = Record;
		this.TopGoal = null;
		this.NodeMap = new HashMap<String, GSNNode>();
		this.DocTagMap = new HashMap<String, String>();
		this.DocHistory = null;
		this.GoalCount = 0;
	}

	GSNDoc Duplicate(String Author, String Role, String Date, String Process) {
		GSNDoc NewDoc = new GSNDoc(this.Record);
		NewDoc.GoalCount = this.GoalCount;
		NewDoc.DocHistory = this.Record.NewHistory(Author, Role, Date, Process,
				NewDoc);
		NewDoc.DocTagMap = this.DuplicateTagMap(this.DocTagMap);
		if (this.TopGoal != null) {
			NewDoc.TopGoal = this.TopGoal.Duplicate(NewDoc, null);
		}
		return NewDoc;
	}

	HashMap<String, String> DuplicateTagMap(HashMap<String, String> TagMap) {
		if (TagMap != null) {
			HashMap<String, String> NewMap = new HashMap<String, String>();
			for (String Key : TagMap.keySet()) {
				NewMap.put(Key, TagMap.get(Key));
			}
			return NewMap;
		}
		return null;
	}

	void UpdateDocHeader() {
		int Revision = TagUtils.GetInteger(this.DocTagMap, "Revision", -1);
		if (Revision != -1) {
			this.DocHistory = this.Record.GetHistory(Revision);
			if (this.DocHistory != null) {
				this.DocHistory.Doc = this;
			}
		}
	}

	public GSNNode GetNode(String Label) {
		return this.NodeMap.get(Label);
	}

	void UncheckAddNode(GSNNode Node) {
		String Key = Node.NodeType + Node.LabelNumber;
		this.NodeMap.put(Key, Node);
	}

	void AddNode(GSNNode Node) {
		String Key = Node.NodeType + Node.LabelNumber;
		GSNNode OldNode = this.NodeMap.get(Key);
		if (OldNode != null) {
			if (MD5.EqualsDigest(OldNode.Digest, Node.Digest)) {
				Node.Created = OldNode.Created;
			}
		}
		this.NodeMap.put(Key, Node);
		if (Node.NodeType == GSNType.Goal) {
			if (Node.GoalLevel == 1) {
				this.TopGoal = Node;
			}
			try {
				int num = Integer.parseInt(Node.LabelNumber);
				if (num > this.GoalCount) {
					this.GoalCount = num;
				}
			} catch (Exception e) {
			}
		}
	}

	private String UniqueNumber(GSNType NodeType, String LabelNumber) {
		GSNNode Node = this.NodeMap.get(WikiSyntax.FormatNodeType(NodeType)
				+ LabelNumber);
		if (Node == null) {
			return LabelNumber;
		}
		return this.UniqueNumber(NodeType, LabelNumber + "'");
	}

	String CheckLabelNumber(GSNNode ParentNode, GSNType NodeType,
			String LabelNumber) {
		if (LabelNumber == null) {
			if (NodeType == GSNType.Goal) {
				this.GoalCount += 1;
				LabelNumber = "" + this.GoalCount;
			} else {
				GSNNode GoalNode = ParentNode.GetCloseGoal();
				GoalNode.SectionCount += 1;
				LabelNumber = GoalNode.LabelNumber + "."
						+ GoalNode.SectionCount;
			}
		}
		return this.UniqueNumber(NodeType, LabelNumber);
	}

	void UpdateNode(GSNNode Node, StringReader Reader, HashMap<String, GSNNode> RefMap) {
		ParserContext Context = new ParserContext(this, Node);
		while (Reader.HasNext()) {
			String Line = Reader.ReadLine();
			if (Line.startsWith(WikiSyntax.VersionDelim)) {
				Reader.LineBack();
				break;
			}
			if (Line.startsWith("*")) {
				Reader.LineBack();
				break;
			}
			if (Line.startsWith("#")) {
				TagUtils.ParseHistoryTag(Context.BaseDoc.Record, Line);
			} else {
				TagUtils.ParseTag(Context.BaseDoc.DocTagMap, Line);
			}
		}
		Context.BaseDoc.UpdateDocHeader();
		GSNNode LastNode = null;
		ArrayList<String> LineList = new ArrayList<String>();
		while (Reader.HasNext()) {
			String Line = Reader.ReadLine();
			if (Line.startsWith(WikiSyntax.VersionDelim)) {
				break;
			}
			if (Line.startsWith("*")) {
				if (Context.IsValidNode(Line)) {
					if (LastNode != null) {
						LastNode.UpdateContent(LineList);
					}
					LineList.clear();
					if (!Context.CheckRefMap(RefMap, Line)) {
						LastNode = Context.AppendNewNode(Line, RefMap);
					} else {
						LastNode = null;
					}
					continue;
				}
			}
			LineList.add(Line);
		}
		if (LastNode != null) {
			LastNode.UpdateContent(LineList);
		}
	}

//	void RemoveNode(GSNNode Node) {
//		assert(this == Node.BaseDoc);
//		if(Node.ParentNode != null) {
//			Node.ParentNode.SubNodeList.remove(o)
//		}
//	}

	
	void AppendNode(GSNNode ParentNode, GSNNode NewNode) {
		
	}
	
	void ReplaceNode(GSNNode Node, GSNNode NewNode) {
		
	}


	void FormatDoc(HashMap<String, GSNNode> NodeRef, StringWriter Stream) {
		if (this.TopGoal != null) {
			this.TopGoal.FormatNode(NodeRef, Stream);
		}
	}
}

class ParserContext {
	GSNDoc BaseDoc;
	ArrayList<GSNNode> GoalStackList;
	GSNNode LastGoalNode;

	ParserContext(GSNDoc BaseDoc, GSNNode BaseNode) {
		this.BaseDoc = BaseDoc;
		this.GoalStackList = new ArrayList<GSNNode>();
		if (BaseNode != null) {
			this.LastGoalNode = BaseNode.GetCloseGoal();
			if (BaseNode.NodeType == GSNType.Goal) {
				this.SetGoalStackAt(BaseNode.GoalLevel, BaseNode);
			}
		} else {
			this.LastGoalNode = null;
		}
	}

	GSNNode GetGoalStackAt(int Level) {
		if (Level < this.GoalStackList.size()) {
			return this.GoalStackList.get(Level);
		}
		return null;
	}

	GSNNode GetParentNodeOfGoal(int Level) {
		if (Level - 1 < this.GoalStackList.size()) {
			GSNNode ParentGoal = this.GoalStackList.get(Level - 1);
			if (ParentGoal != null) {
				return ParentGoal.GetLastNode(GSNType.Strategy);
			}
		}
		return null;
	}

	void SetGoalStackAt(int Level, GSNNode Node) {
		while (this.GoalStackList.size() < Level + 1) {
			this.GoalStackList.add(null);
		}
		this.GoalStackList.set(Level, Node);
	}

	public boolean IsValidNode_(String Line) {
		int Level = WikiSyntax.ParseGoalLevel(Line);
		GSNType NodeType = WikiSyntax.ParseNodeType(Line);
		if (NodeType == GSNType.Goal) {
			GSNNode ParentNode = this.GetParentNodeOfGoal(Level);
			if (ParentNode != null) {
				return true;
			}
			if (Level == 1 && this.LastGoalNode == null) {
				return true;
			}
			return false;
		}
		if (this.LastGoalNode != null) {
			if (NodeType == GSNType.Context) {
				GSNNode LastNode = this.LastGoalNode.GetLastNodeOrSelf();
				if (LastNode.NodeType == GSNType.Context) {
					return false;
				}
				return true;
			}
			if (NodeType == GSNType.Strategy) {
				return !this.LastGoalNode.HasSubNode(GSNType.Evidence);
			}
			if (NodeType == GSNType.Evidence) {
				return !this.LastGoalNode.HasSubNode(GSNType.Strategy);
			}
		}
		return false;
	}

	public boolean IsValidNode(String Line) {
		boolean b = IsValidNode_(Line);
		// System.err.println("IsValidNode? '" + Line + "' ? " + b);
		return b;
	}

	boolean CheckRefMap(HashMap<String, GSNNode> RefMap, String LabelLine) {
		if (RefMap != null) {
			GSNType NodeType = WikiSyntax.ParseNodeType(LabelLine);
			String LabelNumber = WikiSyntax.ParseLabelNumber(LabelLine);
			String RevisionHistory = WikiSyntax.ParseRevisionHistory(LabelLine,
					this.BaseDoc.Record);
			if (LabelNumber != null && RevisionHistory != null) {
				String RefKey = WikiSyntax.FormatRefKey(NodeType, LabelNumber,
						RevisionHistory);
				GSNNode RefNode = RefMap.get(RefKey);
				GSNNode NewNode = null;
				if (RefNode != null) {
					History[] HistoryTriple = WikiSyntax.ParseHistory(
							LabelLine, this.BaseDoc.Record);
					if (NodeType == GSNType.Goal) {
						int Level = WikiSyntax.ParseGoalLevel(LabelLine);
						GSNNode ParentNode = this.GetParentNodeOfGoal(Level);
						NewNode = new GSNNode(this.BaseDoc, ParentNode, Level,
								NodeType, LabelNumber, HistoryTriple);
						this.SetGoalStackAt(Level, NewNode);
						this.LastGoalNode = NewNode;
					} else {
						GSNNode ParentNode = this.LastGoalNode;
						if (NodeType == GSNType.Context) {
							ParentNode = ParentNode.GetLastNodeOrSelf();
						}
						NewNode = new GSNNode(this.BaseDoc, ParentNode,
								ParentNode.GoalLevel, NodeType, LabelNumber,
								HistoryTriple);
					}
					this.BaseDoc.AddNode(NewNode);
					NewNode.NodeDoc = RefNode.NodeDoc;
					NewNode.Digest = RefNode.Digest;
					return true;
				}
			}
		}
		return false;
	}

	public GSNNode AppendNewNode(String LabelLine, HashMap<String, GSNNode> RefMap) {
		GSNType NodeType = WikiSyntax.ParseNodeType(LabelLine);
		String LabelNumber = WikiSyntax.ParseLabelNumber(LabelLine);
		History[] HistoryTriple = WikiSyntax.ParseHistory(LabelLine,
				this.BaseDoc.Record);
		GSNNode NewNode = null;
		if (NodeType == GSNType.Goal) {
			int Level = WikiSyntax.ParseGoalLevel(LabelLine);
			GSNNode ParentNode = this.GetParentNodeOfGoal(Level);
			LabelNumber = this.BaseDoc.CheckLabelNumber(ParentNode, NodeType,
					LabelNumber);
			NewNode = new GSNNode(this.BaseDoc, ParentNode, Level, NodeType,
					LabelNumber, HistoryTriple);
			this.SetGoalStackAt(Level, NewNode);
			this.LastGoalNode = NewNode;
		} else {
			GSNNode ParentNode = this.LastGoalNode;
			if (NodeType == GSNType.Context) {
				ParentNode = ParentNode.GetLastNodeOrSelf();
			}
			LabelNumber = this.BaseDoc.CheckLabelNumber(ParentNode, NodeType,
					LabelNumber);
			NewNode = new GSNNode(this.BaseDoc, ParentNode,
					ParentNode.GoalLevel, NodeType, LabelNumber, HistoryTriple);
		}
		this.BaseDoc.AddNode(NewNode);
		if (RefMap != null && HistoryTriple != null) {
			String RefKey = WikiSyntax.FormatRefKey(NodeType, LabelNumber,
					NewNode.GetHistoryTriple());
			RefMap.put(RefKey, NewNode);
		}
		return NewNode;
	}
}

class GSNRecord {
	ArrayList<History> HistoryList;
	ArrayList<GSNDoc> DocList;
	GSNDoc EditingDoc;

	GSNRecord() {
		this.HistoryList = new ArrayList<History>();
		this.DocList = new ArrayList<GSNDoc>();
		this.EditingDoc = null;
	}

	History GetHistory(int Rev) {
		if (Rev < this.HistoryList.size()) {
			return this.HistoryList.get(Rev);
		}
		return null;
	}

	public History NewHistory(String Author, String Role, String Date,
			String Process, GSNDoc Doc) {
		History History = new History(this.HistoryList.size(), Author, Role,
				Date, Process, Doc);
		this.HistoryList.add(History);
		return History;
	}

	public void AddHistory(int Rev, String Author, String Role, String Date,
			String Process) {
		History History = new History(Rev, Author, Role, Date, Process, null);
		while (!(Rev < this.HistoryList.size())) {
			this.HistoryList.add(null);
		}
		this.HistoryList.set(Rev, History);
	}

	void Parse(String TextDoc) {
		HashMap<String, GSNNode> RefMap = new HashMap<String, GSNNode>();
		StringReader Reader = new StringReader(TextDoc);
		while (Reader.HasNext()) {
			GSNDoc Doc = new GSNDoc(this);
			Doc.UpdateNode(null, Reader, RefMap);
			this.DocList.add(Doc);
		}
		int Rev = 0;
		for (int i = this.DocList.size() - 1; i >= 0; i--) {
			GSNDoc Doc = this.DocList.get(i);
			if (Doc.DocHistory == null) {
				History History = this.GetHistory(Rev);
				if (History == null) {
					String Author = TagUtils.GetString(Doc.DocTagMap, "Author",
							"-");
					String Role = TagUtils
							.GetString(Doc.DocTagMap, "Role", "-");
					String Date = TagUtils
							.GetString(Doc.DocTagMap, "Date", "-");
					String Process = TagUtils.GetString(Doc.DocTagMap,
							"Process", "-");
					History = new History(Rev, Author, Role, Date, Process, Doc);
					while (!(Rev < this.HistoryList.size())) {
						this.HistoryList.add(null);
					}
					this.HistoryList.set(Rev, History);
				}
				Doc.DocHistory = History;
			}
			Rev++;
		}
	}

	// public void Renumber() {
	// HashMap<String, String> LabelMap = new HashMap<String, String>();
	// GSNDoc LatestDoc = this.DocList.get(0);
	// if(LatestDoc.TopGoal != null) {
	// LatestDoc.TopGoal.CreateLabelMap(1, LabelMap);
	// }
	// for(GSNDoc Doc : this.DocList) {
	// if(Doc.TopGoal != null) {
	// Doc.TopGoal.Renumber(LabelMap);
	// }
	// }
	// }

	public void StartToEdit(String Author, String Role, String Date, String Process) {
		if (this.EditingDoc == null) {
			if (this.DocList.size() > 0) {
				this.EditingDoc = this.DocList.get(0).Duplicate(Author, Role,
						Date, Process);
			} else {
				this.EditingDoc = new GSNDoc(this);
				this.EditingDoc.DocHistory = this.NewHistory(Author, Role,
						Date, Process, this.EditingDoc);
			}
		}
	}

	public void Commit() {
		// TODO Auto-generated method stub
	}

	public void Merge(GSNRecord BranchRecord) {
		int CommonRev = this.HistoryList.size();
		boolean IsLinearHistory = true;
		for (int Rev = 0; Rev < BranchRecord.HistoryList.size(); Rev++) {
			History BranchHistory = BranchRecord.GetHistory(Rev);
			History MasterHistory = this.GetHistory(Rev);
			if (MasterHistory == null
					|| !MasterHistory.EqualsHistory(BranchHistory)) {
				if (BranchHistory.Rev != this.HistoryList.size()) {
					IsLinearHistory = false;
				}
				BranchHistory.Rev = this.HistoryList.size();
				this.HistoryList.add(BranchHistory);
			}
		}
		if (IsLinearHistory) {
			for (int i = CommonRev; i < BranchRecord.DocList.size(); i++) {
				GSNDoc BranchDoc = BranchRecord.DocList.get(i);
				BranchDoc.Record = this;
				this.DocList.add(BranchDoc);
			}
		} else {
			ArrayList<GSNNode> ConflictList = new ArrayList<GSNNode>();
			GSNDoc Doc = BranchRecord.GetLatestDoc();
			Doc.TopGoal.Merge(this.GetLatestDoc(), CommonRev, ConflictList);
			// for(GSNNode BranchNode : MergedNodeList) {
			// String Label = BranchNode.GetLabel();
			// GSNNode MasterNode = Doc.NodeMap.get(Label);
			// if(MasterNode != null || MasterNode.Created.Rev ==
			// BranchNode.Created.Rev) {
			// if(MasterNode.IsSameParent(BranchNode)) {
			// if(MasterNode.LastModified.Rev == BranchNode.Branched.Rev) {
			// MasterNode.NodeDoc = BranchNode.NodeDoc;
			// MasterNode.Digest = BranchNode.Digest;
			// MasterNode.HasTag = BranchNode.HasTag;
			// MasterNode.Branched = BranchNode.Branched;
			// MasterNode.LastModified = BranchNode.LastModified;
			// }
			// else {
			// MasterNode.ConflictedWith(BranchNode.NodeDoc);
			// }
			// }
			// }
			// }
		}
	}

	GSNDoc GetLatestDoc() {
		if (this.DocList.size() > 0) {
			this.DocList.get(this.DocList.size() - 1);
		}
		return null;
	}

	public void FormatCase(StringWriter Writer) {
		HashMap<String, GSNNode> RefMap = new HashMap<String, GSNNode>();
		TagUtils.FormatHistoryTag(this.HistoryList, Writer);
		for (int i = 0; i < this.DocList.size(); i++) {
			GSNDoc Doc = this.DocList.get(i);
			Doc.FormatDoc(RefMap, Writer);
			if (i != this.DocList.size() - 1) {
				Writer.println(WikiSyntax.VersionDelim);
			}
		}
	}

}

public class BlueParser {
	public static String ReadFile(String File) {
		StringWriter sb = new StringWriter();
		try {
			BufferedReader br = new BufferedReader(new FileReader(File));
			String Line;
			int linenum = 0;
			while ((Line = br.readLine()) != null) {
				if (linenum > 0) {
					sb.print(StringWriter.LineFeed);
				}
				sb.print(Line);
				linenum++;
			}
			br.close();
		} catch (IOException e) {
			System.err.println("cannot open: " + File);
		}
		return sb.toString();
	}
	
//	public final Srting initData
//		= "*G\n"
//		+ "*C\n"
//		+ "*S\n"
//		+ "**G\n"
//		+ "**G\n"
//		+ "must be G3.\n"
//		+ "**C\n"
//		+ "**G\n";
//	
//	public final static void test() {
//		GSNRecord Record = new GSNRecord();
//		Record.StartToEdit("Robbin", "Master", null, "Planing");
//		GSNDoc Doc = Record.GetEditingDoc();
//		Doc.UpdateNode(null, Reader, RefMap);
//	}
	
	public final static void main(String[] file) {
		String TextDoc = ReadFile(file[0]);
		GSNRecord Record = new GSNRecord();
		Record.Parse(TextDoc);
		// Record.StartToEdit("u", "r", "d", "p");
		// Record.GetEditingNode();
		// Record.ApplyTemplate("Label", Template);
		// Record.Commit();
		// Record.Merge(Record);
		StringWriter Writer = new StringWriter();
		Record.FormatCase(Writer);
		System.out.println("--------\n" + Writer.toString());
	}
}
