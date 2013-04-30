package logParser.Tools;
import java.io.*;
import java.text.*;
import java.util.*;


public class mkTailBat{
	public static void main(String args[]) throws FileNotFoundException, IOException{
		BufferedReader CurrentBR = new BufferedReader(new FileReader("filelengths.dat"));
		BufferedReader historicalBR = new BufferedReader(new FileReader(".."+File.separator+"Historical"+File.separator+"filelengths.dat"));
        StringTokenizer st1;// = new StringTokenizer();
 //       ParsePosition pos = new ParsePosition(0);
        Hashtable CurrentFiles = new Hashtable();
        Hashtable historicalFiles = new Hashtable();
        boolean endOfFile = false;
        while(!endOfFile){
			//System.out.println("in while 1");
			String nextLine= CurrentBR.readLine();
			if(nextLine!=null){
			    st1 = new StringTokenizer(nextLine," ");
			}else{
				st1=null;
			}
			if(st1!=null){
			    if(st1.countTokens() == 2){
			    	String Value = st1.nextToken();
			    	String key = st1.nextToken();
					CurrentFiles.put(key,Value);
				}
		    }else{
				//System.out.println("st1 is null");
				endOfFile = true;
			}
		}
		endOfFile = false;
        while(!endOfFile){
			//System.out.println("while of st2");
			String nextLine= historicalBR.readLine();
			if(nextLine!=null){
			    st1 = new StringTokenizer(nextLine," ");
			}else{
				st1=null;
			}
			if(st1 != null){
				if(st1.countTokens() ==2){
					String Value = st1.nextToken();
					String key = st1.nextToken();
					historicalFiles.put(key,Value);
				}
		    }else{
				endOfFile=true;
			}
		}

		Enumeration en = CurrentFiles.keys();
		while(en.hasMoreElements()){
			String ckey = (String)en.nextElement();
			String cValue = (String)CurrentFiles.get(ckey);
			String hValue = (String)historicalFiles.get(ckey);
			if(hValue != null){
				int rowsToTail = Integer.parseInt(cValue) - Integer.parseInt(hValue);
				if(rowsToTail>0){
					System.out.println("tail -"+rowsToTail+" " + ckey + " >> somefile");
				}
			}else{
				System.out.println("cat "+ckey+" >> somefile");
			}
		}
	}
}