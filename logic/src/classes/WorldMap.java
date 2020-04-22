package classes;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldMap {

    private final int width;
    private final int height;
    List<Station> allStations;
    List<Road> allRoads;

    public WorldMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.allStations = new ArrayList<>();
        this.allRoads = new ArrayList<>();
    }

    public void addNewStation(Station station) throws InstanceAlreadyExistsException{
        validateStation(station);
        this.allStations.add(station);
    }

    public void addNewRoad(Road road) throws InstanceAlreadyExistsException, InstanceNotFoundException{
        validateRoad(road);
        this.allRoads.add(road);
    }

    private void validateStation(Station station) throws InstanceAlreadyExistsException{

        if (!validateCoordinate(station.getCoordinate())) {
            throw new IndexOutOfBoundsException(station.getCoordinate().toString());
        }

        if (this.allStations.contains(station)) {
            throw new InstanceAlreadyExistsException(station.toString());
        }
    }

    private void validateRoad(Road road) throws InstanceAlreadyExistsException, InstanceNotFoundException{
        Station start = road.getStartStation();
        Station end = road.getEndStation();

        if (this.allRoads.contains(road))
            throw new InstanceAlreadyExistsException(road.toString());

        if (!this.allStations.contains(start))
            throw new InstanceNotFoundException(start.toString());

        if (!this.allStations.contains(end)) {
            throw new InstanceNotFoundException(end.toString());
        }
    }

    private boolean validateCoordinate(Coordinate coord){
        return 0 <= coord.getX() &&
                coord.getX() < this.width &&
                0 <= coord.getY() &&
                coord.getY() < this.height;
    }
}
