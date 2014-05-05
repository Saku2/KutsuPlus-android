import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class Pysakit {
 Map<String,StopVO> kutsut=new HashMap<String,StopVO>();	
 public Pysakit()
 {
	 
	 readKutsuStopsFile();
	 readStopsFile();
 }
 
 public void readKutsuStopsFile()
 {
	 try(BufferedReader br = new BufferedReader(new FileReader("c:/testdata/kutsupluspysakit.txt"))) {
	        String line = br.readLine();

	        while (line != null) {
	        	String[] pilkottu=line.split("#");
	        	StopVO svo=new StopVO();
	        	svo.setPyskunta(pilkottu[1].trim());
	        	svo.setKutsuplus(line);
	        	String key=pilkottu[0].trim().substring(1,pilkottu[0].trim().length()-1);
	        	kutsut.put(key, svo);
	            line = br.readLine();
	        }
	        
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
 }

 public void readStopsFile()
 {
	 try(BufferedReader br = new BufferedReader(new FileReader("c:/testdata/stops.txt"))) {
	        String line = br.readLine();

	        while (line != null) {
	        	String[] pilkottu=line.split(",");
	        	StopVO svo=kutsut.get(pilkottu[0].trim());
	        	//System.out.println(line);
	        	if(svo!=null)
	        	{
	        		if(svo.getPyskunta().equals("\"049\""))
	        		  System.out.println("E"+pilkottu[1]+"#"+svo.getKutsuplus().replace("\"", ""));
	        		else
	        		  System.out.println(pilkottu[1]+"#"+svo.getKutsuplus().replace("\"", ""));
	        	}
	        	
	            line = br.readLine();
	        }
	        
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
 }

 
 public static void main(String[] args) {
	new Pysakit();
}
}
