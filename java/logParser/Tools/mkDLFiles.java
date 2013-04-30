package logParser.Tools;
import logParser.FileTool;
import java.io.*;

public class mkDLFiles{
	public static void main(String args[])throws IOException{
		FileTool.buildDataLoaderFile(new File("somefile"), new File("jsperror.DL"));
	}

}

