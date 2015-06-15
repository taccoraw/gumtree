package fr.labri.gumtree.client.ui.tag;

import java.util.List;

import fr.labri.gumtree.client.DiffClient;
import fr.labri.gumtree.client.DiffOptions;
import fr.labri.gumtree.actions.RootAndLeavesClassifier;
import fr.labri.gumtree.actions.TreeClassifier;
import fr.labri.gumtree.matchers.MappingStore;
import fr.labri.gumtree.matchers.Matcher;
import fr.labri.gumtree.tree.Tree;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

public class TagDiff extends DiffClient {

	public TagDiff(DiffOptions diffOptions) {
		super(diffOptions);
	}

	private static void dumpAction(Tree t, int uId, String action, int mId) {
		int start = t.getPos(), end = t.getEndPos();
		String title = tooltip(t), aux = t.getAux();
		if (mId >= 0)
			System.out.printf("%d %d %d %s %d %s %s\n", uId, start, end, action, mId, title, aux);
		else
			System.out.printf("%d %d %d %s NA %s %s\n", uId, start, end, action,      title, aux);
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

		for (Tree t: src.getTrees()) {
			if (c.getSrcMvTrees().contains(t)) {
				mappingIds.put(mappings.getDst(t).getId(), mId);
				dumpAction(t, uId++, "mv", mId++);
			} if (c.getSrcUpdTrees().contains(t)) {
				mappingIds.put(mappings.getDst(t).getId(), mId);
				dumpAction(t, uId++, "upd", mId++);
			} if (c.getSrcDelTrees().contains(t)) {
				dumpAction(t, uId++, "del", -1);
			}
		}

		System.out.println("-----");

		for (Tree t: dst.getTrees()) {
			if (c.getDstMvTrees().contains(t)) {
				int dId = mappingIds.get(t.getId());
				dumpAction(t, uId++, "mv", dId);
			} if (c.getDstUpdTrees().contains(t)) {
				int dId = mappingIds.get(t.getId());
				dumpAction(t, uId++, "upd", dId);
			} if (c.getDstAddTrees().contains(t)) {
				dumpAction(t, uId++, "add", -1);
			}
		}
	}

	private static String tooltip(Tree t) {
		return (t.getParent() != null) ? t.getParent().getTypeLabel() + "/" + t.getTypeLabel() : t.getTypeLabel();
	}

}
