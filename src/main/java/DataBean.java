import java.util.Date;

/**
 * DataBean
 *
 * @author Victoria_Chernenko
 */
public class DataBean {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Date petitionSignTime;

    public DataBean(String firstName, String lastName, String phoneNumber, Date petitionSignTime, Integer index) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.petitionSignTime = petitionSignTime;
        this.index = index;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    private Integer index;

    public DataBean(String firstName, String lastName, String cellPhone, Date birthday) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = cellPhone;
        this.petitionSignTime = birthday;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String cellPhone) {
        this.phoneNumber = cellPhone;
    }

    public Date getPetitionSignTime() {
        return petitionSignTime;
    }

    public void setPetitionSignTime(Date petitionSignTime) {
        this.petitionSignTime = petitionSignTime;
    }
}


