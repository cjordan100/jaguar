
public class Student {

	public int studentId;
	public String major;
	public String gender;
	public int testScore;
	public boolean tookRetake;
	public int retakeScore;
	
	Student(){
		tookRetake = false;
	}
	
	public double getFinalTestScore() {
		
		if(tookRetake == true) {
			return (testScore + retakeScore) / 2;
		}else {
			return testScore;
		}
		
	}
	
}
