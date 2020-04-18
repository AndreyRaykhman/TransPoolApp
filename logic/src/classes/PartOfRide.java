package classes;

import enums.TrempPartType;

import java.util.ArrayList;

public class PartOfRide {

    private Road road;
    private int capacity;
    private ArrayList<Trempist> trempists;

    public PartOfRide(Road road, int capacity) {
        this.road = road;
        this.capacity = capacity;
    }

    public void addTrempist(User user, TrempPartType partType){
        Trempist newTrempist = new Trempist(user, partType);
        trempists.add(newTrempist);
    }
    public int getCapacity() {
        return capacity;
    }

    public ArrayList<Trempist> getTrempists() {
        return trempists;
    }

    public ArrayList<Tremp> getTrempists() {
        return Trempists;
    }

    public int getCapacity() {
        return capacity;
    }

    public Road getRoad() {
        return road;
    }
  
      public void setRoad(Road road) {
        this.road = road;
    }
  
      public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
  
      public void setTrempists(ArrayList<Tremp> trempists) {
        Trempists = trempists;
    }
}

