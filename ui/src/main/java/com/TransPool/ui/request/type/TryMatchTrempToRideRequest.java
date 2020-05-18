package main.java.com.TransPool.ui.request.type;

import main.java.com.TransPool.ui.request.enums.RequestType;
import main.java.com.TransPool.ui.interfaces.UserRequest;

public class TryMatchTrempToRideRequest implements UserRequest {

    int trempRequestID;
    int rideID;
    int maxNumberOfOptions;

    @Override
    public RequestType getRequestType() {

        return RequestType.MATCH_TREMP_TO_RIDE;
    }

    public void setRideID(int rideID) {
        this.rideID = rideID;
    }

    public void setTrempRequestID(int trempRequestID) {
        this.trempRequestID = trempRequestID;
    }

    public void setMaxNumberOfOptions(int maxNumberOfOptions) {
        this.maxNumberOfOptions = maxNumberOfOptions;
    }

    public int getRideID() {
        return rideID;
    }

    public int getTrempRequestID() {
        return trempRequestID;
    }

    public int getMaxNumberOfOptions() {
        return maxNumberOfOptions;
    }
}
