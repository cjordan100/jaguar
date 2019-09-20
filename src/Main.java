import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

public class Main {

	public static void main(String[] args) {
         
		// List of all students 
        Map<Integer, Student> students = new HashMap<Integer, Student>();
        
        // List of female developers
        ArrayList<String> femaleDevelopers = new ArrayList<String>();    
                
        try
        {
        	// File stream to read "Student Info.xlsx"
            FileInputStream file = new FileInputStream(new File("Student Info.xlsx"));
 
            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);
 
            //Get first sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);
 
            //Iterate through each row (student)
            Iterator<Row> rowIterator = sheet.iterator();
            
            boolean firstRow = true;
            while (rowIterator.hasNext())
            {
                Row row = rowIterator.next();
                
                // Checks that it is not the title row
                if(firstRow == false) {
                	
                	Student newStudent = new Student();
                	
                    //For each row, iterate through all the columns
                    Iterator<Cell> cellIterator = row.cellIterator();
                    int cellIndex = 0;
                    while (cellIterator.hasNext())
                    {
                        Cell cell = cellIterator.next();
                        switch (cellIndex)
                        {
                            case 0:
                            	if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            		newStudent.studentId = (int)cell.getNumericCellValue();
                                    cellIndex++;
                            	}
                                break;
                                
                            case 1:
                            	if(cell.getCellType() == Cell.CELL_TYPE_STRING) {
                            		newStudent.major = cell.getStringCellValue();
                                    cellIndex++;
                            	}else {
                            		newStudent.major = null;
                            	}
                                break;
                                
                            case 2:
                            	if(cell.getCellType() == Cell.CELL_TYPE_STRING) {
                                    newStudent.gender = cell.getStringCellValue();
                                    cellIndex++;
                            	}else {
                            		newStudent.gender = null;
                            	}
                                break;
                        }
                        
                    }
                    
                    // Check that the student has complete data before adding to list
                    if(newStudent.studentId != 0 && newStudent.major != null && newStudent.gender != null) {
                    	
                    	// Add the new student to the students list
                        students.put(newStudent.studentId, newStudent);
                        
                        // If student is a computer science major and female, add them to female developers list
                        if(newStudent.major.toLowerCase().equals("computer science") 
                        		&& newStudent.gender.toLowerCase().equals("f")) {
                        	femaleDevelopers.add(Integer.toString(newStudent.studentId));
                        }        
                    }
 		
                }else {
                	firstRow = false;
                	continue;
                }
            }
            
            // Sort the list of female developers
            Collections.sort(femaleDevelopers);
            
            file.close();
            
            // Read test data
            students = importTestScores(students);
            
            // Read retake test data  
            students = importRetakeTestScores(students);
               
            // Put together the json string to send to the server
            JSONObject json = new JSONObject(); 
            json.put("id", "cjjordan234@gmail.com");
            json.put("name", "Casey Jordan");
            json.put("average", calculateClassAverage(students));
            json.put("studentIds", femaleDevelopers.toArray());
            
            String requestPath = "http://3.86.140.38:5000/challenge";
            
            // Create http client
            HttpClient client = HttpClientBuilder.create().build();
            // Create http post
        	HttpPost post = new HttpPost(requestPath);
        	
        	StringEntity entity = new StringEntity(json.toString());
        	post.setEntity(entity);
        	post.setHeader("Accept", "application/json");
        	post.setHeader("Content-type", "application/json");
        	
        	HttpResponse response = client.execute(post);
        	System.out.print(response);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
              
    }
	
	private static Map<Integer, Student> importTestScores(Map<Integer, Student> students){
		
		try
        {
            FileInputStream file = new FileInputStream(new File("Test Scores.xlsx"));
 
            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);
 
            //Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);
 
            //Iterate through each rows (student)
            Iterator<Row> rowIterator = sheet.iterator();
            
            boolean firstRow = true;
            while (rowIterator.hasNext())
            {
                Row row = rowIterator.next();
                if(firstRow == false) {
                	
                    //For each row, iterate through all the columns
                    Iterator<Cell> cellIterator = row.cellIterator();
                    int cellIndex = 0;
                    int currentID = 0;
                    while (cellIterator.hasNext())
                    {
                        Cell cell = cellIterator.next();
                        switch (cellIndex)
                        {
                            case 0:
                            	if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            		currentID = (int)cell.getNumericCellValue();
                            	}
                            	cellIndex++;
                                break;
                                
                            case 1:
                            	if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            		if(students.containsKey(currentID)) {
                            			students.get(currentID).testScore = (int)cell.getNumericCellValue();
                            		}
                            	}
                            	cellIndex++;
                                break;                          
                        }        
                    }
                    
                }else {
                	firstRow = false;
                	continue;
                }
            }
                              
            file.close();            
            
           return students;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        	return students;

        }
		
	}
	
	private static Map<Integer, Student> importRetakeTestScores(Map<Integer, Student> students){
		
		try
        {
            FileInputStream file = new FileInputStream(new File("Test Retake Scores.xlsx"));
 
            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);
 
            //Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);
 
            //Iterate through each rows (student)
            Iterator<Row> rowIterator = sheet.iterator();
            
            boolean firstRow = true;
            while (rowIterator.hasNext())
            {
                Row row = rowIterator.next();
                if(firstRow == false) {
                	
                    //For each row, iterate through all the columns
                    Iterator<Cell> cellIterator = row.cellIterator();
                    int cellIndex = 0;
                    int currentID = 0;
                    while (cellIterator.hasNext())
                    {
                        Cell cell = cellIterator.next();
                        switch (cellIndex)
                        {
                            case 0:
                            	if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            		currentID = (int)cell.getNumericCellValue();
                            	}
                            	cellIndex++;
                                break;
                                
                            case 1:
                            	if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            		if(students.containsKey(currentID)) {
                            			students.get(currentID).tookRetake = true;
                            			students.get(currentID).retakeScore = (int)cell.getNumericCellValue();
                            		}
                            	}
                            	cellIndex++;
                                break;                          
                        }        
                    }
                    
                }else {
                	firstRow = false;
                	continue;
                }
            }
                              
            file.close();            
            
           return students;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        	return students;

        }
		
	}

	private static int calculateClassAverage(Map<Integer, Student> students) {
		
		Set<Map.Entry<Integer, Student>> st = students.entrySet();
		
		double total = 0;
		
		for (Map.Entry<Integer, Student> me:st) 
	       { 
	           total += me.getValue().getFinalTestScore();
	       } 
				
		return (int)Math.rint(total / students.size());
	}
	
}
