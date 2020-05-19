package main.java.com.TransPool.logic.handler;

import enums.DesiredTimeType;
import exception.*;
import jaxb.schema.generated.Path;
import jaxb.schema.generated.Stop;
import jaxb.schema.generated.TransPool;
import jaxb.schema.generated.TransPoolTrip;
import main.java.com.TransPool.logic.map.WorldMap;
import main.java.com.TransPool.logic.map.structure.Road;
import main.java.com.TransPool.logic.traffic.TrafficManager;
import main.java.com.TransPool.logic.traffic.item.Ride;
import main.java.com.TransPool.logic.traffic.item.RideForTremp;
import main.java.com.TransPool.logic.traffic.item.TrempRequest;
import main.java.com.TransPool.logic.map.structure.Station;
import main.java.com.TransPool.logic.traffic.item.SubRide;
import main.java.com.TransPool.logic.user.User;
import main.java.com.TransPool.logic.map.structure.Coordinate;

import javax.management.InstanceAlreadyExistsException;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LogicHandler {
    private WorldMap map;
    private TrafficManager trafficManager;
    private Map<String, User> usersNameToObject;

    public LogicHandler() {
        trafficManager = new TrafficManager();
        usersNameToObject = new HashMap<>();
    }

    public void loadXMLFile(String pathToFile) throws FaildLoadingXMLFileException {

        TransPool transPool = (new XMLHandler(pathToFile)).LoadXML();

        try {
            initWorldMap(transPool);
        } catch (InvalidMapBoundariesException e) {
            throw new FaildLoadingXMLFileException("Failed load XML due to invalid map Boundaries " + "(" + e.getWidth() + "," + e.getLength() + ")");
        }

        initRides(transPool);
    }

    private void initRoads(TransPool transPool) throws FaildLoadingXMLFileException  {

        for (Path path : transPool.getMapDescriptor().getPaths().getPath()) {

            Road toFromRoad = new Road(getStationFromName(path.getFrom()), getStationFromName(path.getTo()));
            toFromRoad.setFuelUsagePerKilometer(path.getFuelConsumption());
            toFromRoad.setLengthInKM(path.getLength());
            toFromRoad.setMaxSpeed(path.getSpeedLimit());
            try {
                map.addNewRoad(toFromRoad);
            } catch (InstanceAlreadyExistsException e) {
                throw new FaildLoadingXMLFileException("Failed load XML due to duplicated roads. Road from" + path.getFrom() + " to " +path.getTo() + " Already Exists ");
            } catch (StationNotFoundException e) {
                throw new FaildLoadingXMLFileException("Failed load XML due to invalid station. Station " + e.getStationName() + " not found ");
            }

            toFromRoad.getStartStation().addRoadFromCurrentStation(toFromRoad);

            if (!path.isOneWay())
            {
                Road fromToRoad = new Road(getStationFromName(path.getTo()), getStationFromName(path.getFrom()));
                fromToRoad.setFuelUsagePerKilometer(path.getFuelConsumption());
                fromToRoad.setLengthInKM(path.getLength());
                fromToRoad.setMaxSpeed(path.getSpeedLimit());
                try {
                    map.addNewRoad(fromToRoad);
                } catch (InstanceAlreadyExistsException e) {
                    throw new FaildLoadingXMLFileException("Failed load XML due to duplicated roads. Road from" + path.getTo() + " to " +path.getFrom() + " Already Exists ");
                } catch (StationNotFoundException e) {
                    throw new FaildLoadingXMLFileException("Failed load XML due to invalid station. Station " + e.getStationName() + " not found ");
                }

                fromToRoad.getStartStation().addRoadFromCurrentStation(fromToRoad);
            }
        }
    }

    private void initStations(TransPool transPool) throws FaildLoadingXMLFileException {

        for (Stop stop : transPool.getMapDescriptor().getStops().getStop()) {
            try {
                map.addNewStation(new Station(new Coordinate(stop.getX(),stop.getY()),stop.getName()));
            } catch (InstanceAlreadyExistsException e) {
                throw new FaildLoadingXMLFileException("Failed load XML due to duplicated stations. Station: " + stop.getName() + " already exists");
            } catch (StationNameAlreadyExistsException e) {
                throw new FaildLoadingXMLFileException("Failed load XML due to duplicated stations. Station name: " + e.getStation().getName() + " already exists");
            } catch (StationAlreadyExistInCoordinateException e) {
                throw new FaildLoadingXMLFileException("Failed load XML due to duplicated stations. Station name: " + e.getStation().getName() + " already exists in coords " + "(" + e.getStation().getCoordinate().getX() +"," +e.getStation().getCoordinate().getY() + ")");
            } catch (StationCoordinateoutOfBoundriesException e) {
                throw new FaildLoadingXMLFileException("Failed load XML due to duplicated stations. Station name: " + e.getStation().getName() + " coords out of Boundaries" + "(" + e.getStation().getCoordinate().getX() +"," +e.getStation().getCoordinate().getY() + ")");
            }
        }
    }

    private void initRides(TransPool transPool) throws FaildLoadingXMLFileException {

        for (TransPoolTrip ride : transPool.getPlannedTrips().getTransPoolTrip()) {

            List<String> roadListStringNames = Arrays.asList(ride.getRoute().getPath().split("\\s*(,)\\s*"));

            Ride newRide;
            try {
                newRide = Ride.createRideFromRoads(new User(ride.getOwner()), map.getRoadsFromStationsNames(roadListStringNames), ride.getCapacity());
            } catch (NoRoadBetweenStationsException e) {
                throw new FaildLoadingXMLFileException("Failed load XML due to invalid road. No road between:" + e.getFromStation() + "to" + e.getToStation());
            } catch (StationNotFoundException e) {
                throw new FaildLoadingXMLFileException("Failed load XML due to invalid station. Station " + e.getStationName() + " not found ");
            }
            newRide.setPricePerKilometer(ride.getPPK());
                newRide.setSchedule(ride.getScheduling().getHourStart(),ride.getScheduling().getDayStart() ,ride.getScheduling().getRecurrences());
                trafficManager.addRide(newRide);
        }
    }

    private void initWorldMap(TransPool transPool) throws InvalidMapBoundariesException, FaildLoadingXMLFileException {

        int width = transPool.getMapDescriptor().getMapBoundries().getWidth();
        int Length = transPool.getMapDescriptor().getMapBoundries().getLength();

        checkMapBoundaries(width,Length);

        map = new WorldMap(width,Length);

        initStations(transPool);
        initRoads(transPool);
    }

    private void checkMapBoundaries(int width, int Length) throws InvalidMapBoundariesException {
        if((width < 6 || width > 100) || (Length < 6 || Length > 100))
            throw new InvalidMapBoundariesException(width,Length);
    }

    public Ride createNewEmptyRide(User rideOwner, List<Road> roads, int capacity){
        return Ride.createRideFromRoads(rideOwner, roads, capacity);
    }

    public List<RideForTremp> getAllPossibleTrempsForTrempRequest(TrempRequest trempRequest){
        Station start = trempRequest.getStartStation();
        Station end = trempRequest.getEndStation();
        int maxNumberOfConnections = trempRequest.getMaxNumberOfConnections();

        List<RideForTremp> relevantByRouteOptions = trafficManager.getRideOptions(maxNumberOfConnections, start, end);
        relevantByRouteOptions.sort(Comparator.comparingInt(RideForTremp::getNumOfParts));

        Function<RideForTremp, LocalTime> rideCorrectTimeGetter = trempRequest.getDesiredTimeType() == DesiredTimeType.DEPART ?
                RideForTremp::getDepartTime :  RideForTremp::getArriveTime;

        return relevantByRouteOptions.stream()
                .filter(rideForTremp -> rideCorrectTimeGetter.apply(rideForTremp).equals(trempRequest.getDesiredTime()))
                .collect(Collectors.toList());

    }

    public void addRide(Ride rideToAdd){
        this.trafficManager.addRide(rideToAdd);
    }

    public TrempRequest createNewEmptyTrempRequest(Station start, Station end) throws NoPathExistBetweenStationsException{

        if (!start.canReachStation(end)){
            throw new NoPathExistBetweenStationsException();
        }
        return new TrempRequest(start, end);
    }

    public void addTrempRequest(TrempRequest trempRequest){
        this.trafficManager.addTrempRequest(trempRequest);
    }

    public Station getStationFromName(String name) throws StationNotFoundException {
        return this.map.getStationByName(name);
    }

    public User getUserByName(String name){
        if (!usersNameToObject.containsKey(name)) {
            usersNameToObject.put(name, new User(name));
        }
        return usersNameToObject.get(name);
    }

    public List<Ride> getAllRides(){
        return this.trafficManager.getAllRides();
    }


    public List<TrempRequest> getAllTrempRequests(){
        return this.trafficManager.getAllTrempRequests();
    }

    public List<TrempRequest> getAllNonMatchedTrempRequests(){
        return getAllTrempRequests().stream()
                .filter(TrempRequest::isNotAssignedToRides)
                .collect(Collectors.toList());

    }


    public TrempRequest getTrempRequestById(int trempID) throws TrempRequestNotExist{
        return this.trafficManager.getTrempRequestById(trempID);
    }

    public Ride getRideById(int rideID) throws RideNotExistsException{
        return this.trafficManager.getRideByID(rideID);
    }

    public List<Station> getAllStations(){
        return this.map.getAllStations();
    }

    public List<Road> getRoadsFromStationsNames(List<String> stationNames) throws NoRoadBetweenStationsException {
        return this.map.getRoadsFromStationsNames(stationNames);
    }
}
