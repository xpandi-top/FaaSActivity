package basic;

public class Request {
    String objectName;
    String studentID;
    int startIndex;
    public Request(){

    }
    public Request(String objectName){
        this.objectName = objectName;
    }
    public Request(String objectName, String studentID, int startIndex){
        this.objectName = objectName;
        this.studentID = studentID;
        this.startIndex = startIndex;
    }
    public void setObjectName(String objectName){
        this.objectName = objectName;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }
    public void setStartIndex(int startIndex){this.startIndex = startIndex;}
    public String getStudentID(){
        return this.studentID;
    }

    public String getObjectName() {
        return this.objectName;
    }
    public int getStartIndex(){return this.startIndex;}
}
