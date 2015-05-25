package fr.labri.gumtree.client.ui.tag;

import java.util.List;

import fr.labri.gumtree.client.DiffClient;
import fr.labri.gumtree.client.DiffOptions;
import fr.labri.gumtree.actions.RootAndLeavesClassifier;
import fr.labri.gumtree.actions.TreeClassifier;
import fr.labri.gumtree.algo.StringAlgorithms;
import fr.labri.gumtree.matchers.MappingStore;
import fr.labri.gumtree.matchers.Matcher;
import fr.labri.gumtree.tree.Tree;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

public class TagDiff extends DiffClient {

	private static final String SRC_MV_SPAN = "<span class=\"%s\" id=\"move-src-%d\" data-title=\"%s\">";
	private static final String DST_MV_SPAN = "<span class=\"%s\" id=\"move-dst-%d\" data-title=\"%s\">";
	private static final String ADD_DEL_SPAN = "<span class=\"%s\" data-title=\"%s\">";
	private static final String UPD_SPAN = "<span class=\"cupd\">";
	private static final String ID_SPAN = "<span class=\"marker\" id=\"mapping-%d\"></span>";
	private static final String END_SPAN = "</span>";

	private class Pos2Tag {
		public void addTags(int pos, String startTag, int endPos, String endTag) {
			addStartTag(pos, startTag);
			addEndTag(endPos, endTag);
		}
		public void addStartTag(int pos, String tag) {
			System.out.printf("%d %s %s\n", pos, "start", tag);
		}
		public void addEndTag(int pos, String tag) {
			System.out.printf("%d %s %s\n", pos, "end", tag);
		}
	}

	public TagDiff(DiffOptions diffOptions) {
		super(diffOptions);
	}

	@Override
	public void start() {
		Matcher matcher = getMatcher();
		Tree src = matcher.getSrc(), dst = matcher.getDst();
		MappingStore mappings = matcher.getMappings();

		TreeClassifier c = new RootAndLeavesClassifier(src, dst, matcher);
		TIntIntMap mappingIds = new TIntIntHashMap();
		int uId = 1;
		int mId = 1;

		Pos2Tag ltags = new Pos2Tag();
		for (Tree t: src.getTrees()) {
			if (c.getSrcMvTrees().contains(t)) {
				mappingIds.put(mappings.getDst(t).getId(), mId);
				ltags.addStartTag(t.getPos(), String.format(ID_SPAN, uId++));
				ltags.addTags(t.getPos(), String.format(SRC_MV_SPAN, "token mv", mId++, tooltip(t)), t.getEndPos(), END_SPAN);
			} if (c.getSrcUpdTrees().contains(t)) {
				mappingIds.put(mappings.getDst(t).getId(), mId);
				ltags.addStartTag(t.getPos(), String.format(ID_SPAN, uId++));
				ltags.addTags(t.getPos(), String.format(SRC_MV_SPAN, "token upd", mId++, tooltip(t)), t.getEndPos(), END_SPAN);
				
				List<int[]> hunks = StringAlgorithms.hunks(t.getLabel(), mappings.getDst(t).getLabel());
				for(int[] hunk: hunks)
					ltags.addTags(t.getPos() + hunk[0], UPD_SPAN, t.getPos() + hunk[1], END_SPAN);
				
			} if (c.getSrcDelTrees().contains(t)) {
				ltags.addStartTag(t.getPos(), String.format(ID_SPAN, uId++));
				ltags.addTags(t.getPos(), String.format(ADD_DEL_SPAN, "token del", tooltip(t)), t.getEndPos(), END_SPAN);
			}
		}

		System.out.println("-----");

		Pos2Tag rtags = new Pos2Tag();
		for (Tree t: dst.getTrees()) {
			if (c.getDstMvTrees().contains(t)) {
				int dId = mappingIds.get(t.getId());
				rtags.addStartTag(t.getPos(), String.format(ID_SPAN, uId++));
				rtags.addTags(t.getPos(), String.format(DST_MV_SPAN, "token mv", dId, tooltip(t)), t.getEndPos(), END_SPAN);
			} if (c.getDstUpdTrees().contains(t)) {
				int dId = mappingIds.get(t.getId());
				rtags.addStartTag(t.getPos(), String.format(ID_SPAN, uId++));
				rtags.addTags(t.getPos(), String.format(DST_MV_SPAN, "token upd", dId, tooltip(t)), t.getEndPos(), END_SPAN);
				List<int[]> hunks = StringAlgorithms.hunks(mappings.getSrc(t).getLabel(), t.getLabel());
				for(int[] hunk: hunks)
					rtags.addTags(t.getPos() + hunk[2], UPD_SPAN, t.getPos() + hunk[3], END_SPAN);
			} if (c.getDstAddTrees().contains(t)) {
				rtags.addStartTag(t.getPos(), String.format(ID_SPAN, uId++));
				rtags.addTags(t.getPos(), String.format(ADD_DEL_SPAN, "token add", tooltip(t)), t.getEndPos(), END_SPAN);
			}
		}
	}

	private static String tooltip(Tree t) {
		return (t.getParent() != null) ? t.getParent().getTypeLabel() + "/" + t.getTypeLabel() : t.getTypeLabel();
	}

}
