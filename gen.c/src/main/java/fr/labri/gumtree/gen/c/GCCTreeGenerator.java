package fr.labri.gumtree.gen.c;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import fr.labri.gumtree.io.TreeGenerator;
import fr.labri.gumtree.io.TreeIoUtils;
import fr.labri.gumtree.tree.Tree;

public class GCCTreeGenerator extends TreeGenerator {

	@Override
	public Tree generate(String file) {
		return TreeIoUtils.fromXmlFile(file + ".gcc");
	}

	@Override
	public boolean handleFile(String file) {
		return file.toLowerCase().endsWith(".c");
	}

	@Override
	public String getName() {
		return "gccdiff";
	}
}
