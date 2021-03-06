package transpool.ui.request.type;

import transpool.ui.request.enums.RequestType;
import transpool.ui.interfaces.UserRequest;

public class NewTrempRequest implements UserRequest {
    private String userName;
    private String fromStation;
    private String toStation;
    private String chosenTime;
    private int departDay = 1;
    private String desiredTimeType;
    private boolean directOnly;
    private int diffMinutes = 0;

    @Override
    public RequestType getRequestType() {
        return RequestType.NEW_TREMP;
    }

    public void setDirectOnly(boolean directOnly) {
        this.directOnly = directOnly;
    }

    public void setDepartDay(int departDay) {
        this.departDay = departDay;
    }

    public void setChosenTime(String chosenTime) {
        this.chosenTime = chosenTime;
    }

    public void setFromStation(String fromStation) {
        this.fromStation = fromStation;
    }

    public void setToStation(String toStation) {
        this.toStation = toStation;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setDesiredTimeType(String desiredTimeType) {
        this.desiredTimeType = desiredTimeType;
    }

    public void setDiffMinutes(int diffMinutes) {
        this.diffMinutes = diffMinutes;
    }

    public int getDepartDay() {
        return departDay;
    }

    public String getChosenTime() {
        return chosenTime;
    }

    public String getFromStation() {
        return fromStation;
    }

    public String getToStation() {
        return toStation;
    }

    public String getUserName() {
        return userName;
    }

    public String getDesiredTimeType() {
        return desiredTimeType;
    }

    public int getDiffMinutes() {
        return diffMinutes;
    }
}
