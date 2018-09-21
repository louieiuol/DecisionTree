package cmsc420.meeshquest.part1;
import static org.junit.Assert.*;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.junit.Test;









import cmsc420.drawing.CanvasPlus;


public class CityDictionaryTest {

	@Test
	public void test() throws IOException {
		CanvasPlus canvas = new CanvasPlus("golub123", 64,64);
		/*City ny = new City("New York","blue", 55, 55,25);
		City queens= new City("Queens","blue",500,500,1000);
		City brooklyn = new City("Brooklyn","blue", 92,92,1000);
		City bronx = new City("bronx","blue", 501,501,1000);
		City longisland = new City("Long Island","blue",99,99,1000);
		City newark = new City("Newark","blue", 20,20 ,100);
		City jersey = new City("Jersey City","blue", 29,28, 100); 
		City hamptons = new City("Hamptons","blue", 12,75, 100);
		City island = new City("Long Island","blue", 72,28,100);
		City baltimore = new City("Baltimore","black", 76,39,30);
		City chicago = new City("Chicago","blue",81,47,35);*/
		City atlanta = new City("Atlanta","red",42,20,40);
		City miami = new City("Miami","black",40,22,30);
		PRQuadTree tree = new PRQuadTree(64,64,canvas);
		Dictionary dictionary = new Dictionary();
		dictionary.add(atlanta);
		dictionary.add(miami);
		Float k = 98.0f;
		Float k1 = 98.0f;
		tree.insert(atlanta);
		tree.insert(miami);
		/*tree.insert(chicago);
		tree.insert(atlanta);
		tree.insert(baltimore);
		tree.insert(miami);
		tree.delete(miami);
		tree.insert(queens);
		tree.insert(bronx);
		tree.makeBlackList(tree.root);
		//System.out.println(tree.blacklist.toString());
		//System.out.println(tree.blacklist.size());
		NameComparator comp = new NameComparator();
		StringDictionary result = new StringDictionary(comp);
		AvlGTree<String,City> avl = new AvlGTree<String,City>(comp,1);
		avl.put(chicago.getName(), chicago);
		avl.put(atlanta.getName(),atlanta);
		avl.put(hamptons.getName(),hamptons);
		tree.delete(chicago);
		tree.makeBlackList();
		System.out.println(tree.blacklist.toString());
		System.out.println(avl.size());
		
		//avl.putAll(dictionary.sd);
		System.out.println(avl.height());
		//System.out.println(avl.toString());
		for(Map.Entry<String,City> entry : avl.entrySet()){
			System.out.println(entry.toString());
		}
		
		
		result = tree.inRange(80, 28, 30);
		//System.out.println("Cities In range are as follows: ");
		//System.out.println(result.toString());
		//City n =tree.closestCity(85, 26);
		//System.out.println("The closest city is " + n.toString());
		//tree.makeRoads();
			//tree.insert(b);
		//tree.insert(g);
		//tree.insert(q);
		/*tree.insert(hamptons);
		tree.insert(island);
		tree.insert(t);*/
		//tree.delete(atlanta);
		//System.out.println(tree.root);
		//tree.toList(tree.root);
		//System.out.println(tree.list);
		//System.out.println(tree.list.size());
		//PRNode p2 = tree.search(98, 98);
		//System.out.println(p2);
		/*tree.deleteHelper(98, 98, tree.root);
		tree.deleteHelper(29, 28, tree.root);
		tree.deleteHelper(29, 28, tree.root);*/
		
		//PRNode p3 = tree.search(98, 98);
		//System.out.println(p3);*/
		canvas.save("gooool");
		canvas.dispose();
	}

}
