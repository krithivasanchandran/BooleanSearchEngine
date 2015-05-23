import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class BooleanRetrieval {
	
	HashMap<String, Set<Integer>> invIndex;
	int [][] docs;
	HashSet<String> vocab;
	HashMap<Integer, String> map;  // int -> word
	HashMap<String, Integer> i_map; // inv word -> int map

	public BooleanRetrieval() throws Exception{
		// Initialize variables and Format the data using a pre-processing class and set up variables
		invIndex = new HashMap<String, Set<Integer>>();
		DatasetFormatter formater = new DatasetFormatter();
		formater.textCorpusFormatter("./all.txt");
		docs = formater.getDocs();
		vocab = formater.getVocab();
		map = formater.getVocabMap();
		i_map = formater.getInvMap();
	}

	void createPostingList(){
		//Initialze the inverted index with a SortedSet (so that the later additions become easy!)
		for(String s:vocab){
			invIndex.put(s, new TreeSet<Integer>());
		}
		//for each doc
		for(int i=0; i<docs.length; i++){
			//for each word of that doc
			for(int j=0; j<docs[i].length; j++){
				//Get the actual word in position j of doc i
				String w = map.get(docs[i][j]);
				if(invIndex.containsKey(w)){
				invIndex.get(w).add(i+1);
				}else{
					System.out.println(" Actual Word in position j of doc i is not found in invIndex Map");
				}
				
				/* TO-DO:
				Get the existing posting list for this word w and add the new doc in the list. 
				Keep in mind doc indices start from 1, we need to add 1 to the doc index , i
				 */
			}

		}
	}

	Set<Integer> intersection(Set<Integer> a, Set<Integer> b){
		/*
		First convert the posting lists from sorted set to something we 
		can iterate easily using an index. I choose to use ArrayList<Integer>.
		Once can also use other enumerable.
		 */
		ArrayList<Integer> PostingList_a = new ArrayList<Integer>(a);
		ArrayList<Integer> PostingList_b = new ArrayList<Integer>(b);
		TreeSet result = new TreeSet();

		//Set indices to iterate two lists. I use i, j
		int i = 0;
		int j = 0;

		while(i!=PostingList_a.size() && j!=PostingList_b.size()){
		
			//TO-DO: Implement the intersection algorithm here

			if(Integer.compare(PostingList_a.get(i),PostingList_b.get(j)) == 0){
				result.add(PostingList_a.get(i));
				i++;j++;
			}else if(Integer.compare(PostingList_a.get(i),PostingList_b.get(j)) < 0){
				i++;
			}else{
				j++;
			}
		}
		return result;
	}

	Set <Integer> evaluateANDQuery(String a, String b) throws NullPointerException{
			return intersection(invIndex.get(a),invIndex.get(b));
	}

	Set<Integer> union(Set<Integer> a, Set<Integer> b){
		/*
		 * This is very simple if we use Java Collections and its methods
		 * TO-DO: Figure out how to perform union?
		 */
		
		ArrayList<Integer> PostingUnionList_a = new ArrayList<Integer>(a);
		ArrayList<Integer> PostingUnionList_b = new ArrayList<Integer>(b);
		TreeSet result = new TreeSet();
		// Implement Union here
		
		for(Integer postingUnion_a: PostingUnionList_a){
			result.add(postingUnion_a);
		}
		for(Integer postingUnion_b: PostingUnionList_b){
			result.add(postingUnion_b);
		}
		return result;
	}

	Set <Integer> evaluateORQuery(String a, String b){
		return union(invIndex.get(a), invIndex.get(b));
	}
	
	Set<Integer> not(Set<Integer> a){
		TreeSet result = new TreeSet();
		/*
		 Hint:
		 NOT is very simple. I traverse the sorted posting list between i and i+1 index
		 and add the other (NOT) terms in this posting list between these two pointers
		 First convert the posting lists from sorted set to something we 
		 can iterate easily using an index. I choose to use ArrayList<Integer>.
		 Once can also use other enumerable.
		 */
		
		ArrayList<Integer> PostingList_a = new ArrayList<Integer>(a);
		int total_docs = docs.length; // 400 to docs.length
		// TO-DO: Implement the not method using above idea or anything you find better!
		
		for(int i=0;i<PostingList_a.size() && (i+1) < PostingList_a.size();i++){
			
			if(i == 0 && PostingList_a.get(i) != 1){
				int incrementor = 1;
				 while(incrementor != PostingList_a.get(i)){
					result.add(incrementor);
					incrementor = incrementor + 1;
				 }
			}
			
			if(i == PostingList_a.size()-1 && PostingList_a.get(i) != total_docs){
				int endlimiter = PostingList_a.get(i);
				while(endlimiter != total_docs){
					result.add(endlimiter);
					endlimiter = endlimiter + 1;
				 }
			}
			
			int first = PostingList_a.get(i);
			int second = PostingList_a.get(i+1);
			while(first != (second-1)){
				first = first + 1;
				result.add(first);
			}
		}
		return result;
	}

	Set <Integer> evaluateNOTQuery(String a){
		return not(invIndex.get(a));
	}
	
	Set <Integer> evaluateAND_NOTQuery(String a, String b){
		return intersection(invIndex.get(a), not(invIndex.get(b)));
	}
	
	public static void main(String[] args) throws Exception {

		//Initialize parameters
		BooleanRetrieval model = new BooleanRetrieval();

		//Generate posting lists
		model.createPostingList();

		//Print the posting lists from the inverted index
		String caseSelector = args[0];
		String outputFilePath = null;
		int tt = 0;
		int tracker = 0;
		
		for(String t: args){
			if(t.contains(".txt")){
				outputFilePath = t;
				tracker = tt;
			}
			tt++;
		}
		
		if(outputFilePath.isEmpty() || outputFilePath == null){
		   System.out.println(" Please enter the output file path");
		}
		StringBuffer buffer  = new StringBuffer();
		for(int b = 1;b<tracker;b++){
			 buffer.append(args[b]);
			 buffer.append(" ");
		}
		
		File file = new File("C:/Users/KRITHIVASAN CHANDRAN/Desktop/"+outputFilePath);
		BufferedWriter  writer = new BufferedWriter(new FileWriter(file));
		
		switch(caseSelector)
		{
			case "PLIST":
				boolean flag = false;
				for(String s : model.invIndex.keySet()){
					if(s.equalsIgnoreCase(args[1])){
						writer.write(s + " -> " + model.invIndex.get(s));						
						flag = true;
					}}
				if(!flag){
					writer.write(args[1] + " -> []");
				}
				writer.flush();
				writer.close();
			break;
				
			case "AND":
				String str = buffer.toString();
				boolean flag_and = false;
				if(str != null && !str.isEmpty() && str.contains(" ")){
					String[] a = str.split(" ");
					for(String s: a){
						if(s.trim().equalsIgnoreCase("and") && !a[2].isEmpty()){
							writer.append(a[0] + " AND " + a[2] + " -> ");
							try{
							Set <Integer> and_set =  model.evaluateANDQuery(a[0],a[2]);
							int counter = 0;
								 for(Integer aa: and_set){
										
									 if(counter == 0){
										 writer.append(" [");
									 }
									 writer.append(aa+"");
									 if(counter != and_set.size()-1){
									 writer.append(", ");
									 }
									 if(counter == and_set.size()-1){
										 writer.append("]"+"");
									 }
									 counter++;
									 flag_and = true;
								 }
							}catch(Exception ex){
								flag_and = false;
							}
						
						}}}
				
					if(!flag_and){
						writer.write("[]");
					}
					writer.flush();
					writer.close();
				break;
			
			case "OR":
				String str_OR = buffer.toString();
				boolean flag_or = false;
				if(str_OR != null && !str_OR.isEmpty() && str_OR.contains(" ")){
					String[] a = str_OR.split(" ");
					for(String s: a){
						if(s.trim().equalsIgnoreCase("or") && !a[2].isEmpty()){
							writer.append(a[0] + " OR " + a[2] + " -> ");
							try{
								Set<Integer> or_set =  model.evaluateORQuery(a[0],a[2]);
								int or_counter = 0;
								 for(Integer bb: or_set){
										
									 if(or_counter == 0){
										 writer.append(" [");
									 }
									 writer.append(bb+"");
									 if(or_counter != or_set.size()-1){
									 writer.append(", ");
									 }
									 if(or_counter == or_set.size()-1){
										 writer.append("]"+"");
									 }
									 or_counter++;
									 flag_or = true;
									 }
							}catch(Exception e){
								flag_or = false;
							}}}}
							if(!flag_or){
								writer.write("[]");
							}
							writer.flush();
							writer.close();
						break;
				
			case "AND-NOT":
				String str_And_Not = buffer.toString();
				boolean flag_and_not = false;
				if(str_And_Not.contains(" ")){
					String[] a = str_And_Not.split(" ");
					for(String s: a){
					 if(s.equalsIgnoreCase("and")){
						if(a[2].contains("(") && a[2].contains("NOT") && !a[3].isEmpty() && a[3].contains(")")){
							writer.append(str_And_Not + " -> ");
							try{
								Set<Integer> p_and_not = model.evaluateAND_NOTQuery(a[0], a[3].replace(")",""));
								int and_not_counter = 0;
								for(Integer andNot_bb : p_and_not){
									
									 if(and_not_counter == 0){
										 writer.append(" [");
									 }
									 writer.append(andNot_bb+"");
									 if(and_not_counter != p_and_not.size()-1){
									 writer.append(", ");
									 }
									 if(and_not_counter == p_and_not.size()-1){
										 writer.append("]"+"");
									 }
									 and_not_counter++;
									 flag_and_not = true;
								}
							}catch(Exception e){
								flag_and_not = false;
							}
							
					 }}}}
						if(!flag_and_not){
							writer.write("[]");
						}
				writer.flush();
				writer.close();
				break;
		}
	}
}