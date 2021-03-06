package main.window.main;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.window.main.sub.map.DynamicMapController;
import main.window.main.sub.ride.RideSubWindowController;
import main.window.main.sub.tremp.TrempSubWindowController;
import main.window.main.task.assainTrempTask;
import main.window.newride.createRideController;
import main.window.newtremp.CreateTrempController;
import main.window.newxmlload.newXmlLoadController;
import tasks.loadFile.loadXmlFileTask;
import transpool.logic.handler.LogicHandler;
import transpool.logic.map.structure.Road;
import transpool.logic.traffic.item.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;



public class MainWindowController {

    private LogicHandler logicHandler;
    private Stage primaryStage;

    @FXML private Pane rideComponent;
    @FXML private Pane trempComponent;
    @FXML private Pane mapComponent;
    @FXML private RideSubWindowController rideComponentController;
    @FXML private TrempSubWindowController trempComponentController;
    @FXML private DynamicMapController mapComponentController;

    @FXML
    private Button loadXmlBtn;

    @FXML
    private Button newBtn;

    @FXML
    private Button newBtn1;

    @FXML
    private Button matchBtn;

    @FXML
    private Button matchBtn1;

    @FXML
    void onLoadXmlBtnClick(ActionEvent event) throws IOException {
        loadXmlFile();
    }

    public void loadXmlFile() throws IOException {
        Stage stage = new Stage();
        stage.setResizable(false);
        URL resource = getClass().getResource("/main/window/newxmlload/newXmlLoadWindow.fxml");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(resource);
        Parent root = loader.load();
        newXmlLoadController controller = loader.getController();
        controller.setMainController(this);
        controller.setStage(stage);
        //controller.setLogicHandler(logicHandler);
        Scene scene = new Scene(root, 500, 500);

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.show();
    }


    public void createNewRide() throws IOException{
        Stage stage = new Stage();
        stage.setResizable(false);
        URL resource = getClass().getResource("/main/window/newride/newRideWindow.fxml");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(resource);
        Parent root = loader.load();
        createRideController controller = loader.getController();
        controller.setMainController(this);
        controller.setStage(stage);
        controller.setLogicHandler(logicHandler);
        Scene scene = new Scene(root, 500, 700);

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void onQuitBtnClick(ActionEvent event) { primaryStage.close(); }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setLogic(LogicHandler logicHandler) { this.logicHandler = logicHandler;  }

    @FXML
    public void initialize(){
        if (rideComponentController != null && this.trempComponentController != null && mapComponentController != null){
            this.rideComponentController.setMainController(this);
            this.trempComponentController.setMainController(this);
            this.mapComponentController.setMainController(this);
        }
    }

    public void updateRidesList(){
        this.rideComponentController.updateRidesList();
    }

    public void updateTrempsList(){
        this.trempComponentController.updateTrempsList();
    }

    public void createNewTremp() throws IOException {
        Stage stage = new Stage();
        stage.setResizable(false);
        URL resource = getClass().getResource("/main/window/newtremp/newTrempWindow.fxml");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(resource);
        Parent root = loader.load();
        CreateTrempController controller = loader.getController();
        controller.setMainController(this);
        controller.setStage(stage);
        controller.setLogicHandler(logicHandler);
        Scene scene = new Scene(root, 500, 500);

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.show();
    }

    public List<Ride> getAllRides() {
        return this.logicHandler.getAllRides();
    }

    public List<TrempRequest> getAllTrempRequests(){
        return this.logicHandler.getAllTrempRequests();
    }

    public void updateMapWithStationsAndRoads(){
        mapComponentController.initVisualMap(this.logicHandler.getMap());
    }

//    public void updateMapWithRide(Ride ride, LocalDateTime currentDateTime){
//        List<Road> roadsToMark = ride.getPartsOfRide().stream().map(PartOfRide::getRoad).collect(Collectors.toList());
//        this.mapComponentController.markRoads(roadsToMark); //markRoads()
//
//        List<Road> roadsToHide = new LinkedList<>();
//        for (Road toMark: roadsToMark){
//            for (Road toHide: this.logicHandler.getAllRoads()){
//                if (!roadsToMark.contains(toHide) && toHide.sharesOppositeStations(toMark))
//                    roadsToHide.add(toHide);
//            }
//        }
//
//        this.mapComponentController.hideRoads(roadsToHide);
//    }

    public void updateMapRoadsByRides(List<Ride> rides){
        List<Road> allRoadsToMark = rides.stream().map(Ride::getAllRoads).flatMap(List::stream).collect(Collectors.toList());


        rides.forEach(ride -> mapComponentController.markStations(ride.getStartStation(), ride.getEndStation()));
        this.mapComponentController.markRoadsInRed(allRoadsToMark);

        List<Road> roadsToHide = new LinkedList<>();
        List<Road> leftRoads = this.logicHandler.getAllRoads().stream().filter(rode -> !allRoadsToMark.contains(rode)).collect(Collectors.toList());
        for (Road toHide: leftRoads){
            for (Road toMark: allRoadsToMark){
                if (!allRoadsToMark.contains(toHide) && toHide.sharesOppositeStations(toMark))
                    roadsToHide.add(toHide);
            }
        }
        this.mapComponentController.hideRoads(roadsToHide);
    }

    public void updateMapWithRidesRunningOn(LocalDateTime currentDateTime) {
        List<Ride> ridesRunningNow = this.logicHandler.getRidesRunningOn(currentDateTime);
        this.mapComponentController.unMarkRedMarkedEdges();
        this.mapComponentController.unMarkBlueMarkedEdges();
        this.mapComponentController.removeTextFromEdges();
        this.mapComponentController.unMarkAllStations();
        this.mapComponentController.clearAllStationsData();

        markRidesStations(ridesRunningNow);
        updateMapRoadsByRides(ridesRunningNow);
        updateMapWithRideStatus(ridesRunningNow, currentDateTime);
        updateMapRoadsTextByRides(ridesRunningNow, currentDateTime);

        ridesRunningNow.forEach(ride -> updateMapStationsWithPartsOfRides(ride.getPartsOfRide(), currentDateTime));
    }

    private void markRidesStations(List<Ride> ridesRunningNow) {
        ridesRunningNow.forEach(ride -> this.mapComponentController.markStations(ride.getStartStation(), ride.getEndStation()));
    }

    private void updateMapWithRideStatus(List<Ride> ridesRunningNow, LocalDateTime currentDateTime) {
        List<Road> progressRoadsToMark = new LinkedList<>();

        for (Ride ride: ridesRunningNow){
            for(PartOfRide partOfRide: ride.getPartsOfRide()){
                progressRoadsToMark.add(partOfRide.getRoad());
                if(partOfRide.getSchedule().hasInstanceContainingDateTime(currentDateTime)) {
                    break;
                }
            }
        }

        this.mapComponentController.markRoadsInBlue(progressRoadsToMark); //mark with different color
        hideOppositeRoads(progressRoadsToMark);

    }

    //TODO this code was duplicated in other function, use this instead
    private void updateMapRoadsTextByRides(List<Ride> ridesRunningNow, LocalDateTime currentDateTime) {
        Map<Road, String> roads2Messages  = ridesRunningNow.stream()
                .map(Ride::getPartsOfRide)
                .flatMap(Collection::stream)
                .filter(partOfRide -> partOfRide.getSchedule().hasInstanceContainingDateTime(currentDateTime))
                .collect(Collectors.toMap(
                        PartOfRide::getRoad,
                        partOfRide -> String.format("%s-%s(%d)", partOfRide.getSchedule().getStartTime(), partOfRide.getSchedule().getEndTime(), partOfRide.getTrempistsManager().getAllTrempists().size())
                ));
        this.mapComponentController.updateEdgesWithTexts(roads2Messages);
    }

    private void hideOppositeRoads(List<Road> roadsToColor){
        List<Road> roadsToHide = new LinkedList<>();
        List<Road> leftRoads = this.logicHandler.getAllRoads().stream().filter(rode -> !roadsToColor.contains(rode)).collect(Collectors.toList());
        for (Road toHide: leftRoads){
            for (Road toMark: roadsToColor){
                if (!roadsToColor.contains(toHide) && toHide.sharesOppositeStations(toMark))
                    roadsToHide.add(toHide);
            }
        }
        this.mapComponentController.hideRoads(roadsToHide);
    }

    public void switchLiveMapOn() {
        this.trempComponentController.updateMatchText();
        this.rideComponentController.clearSelection();
        this.trempComponentController.clearSelection();
        this.rideComponentController.updateRidesList();
        this.trempComponentController.updateTrempsList();
        this.mapComponentController.unMarkAllStations();

        this.mapComponentController.toggleLiveMapOn();
    }

    public void clearView(){
        this.rideComponentController.clear();
        this.trempComponentController.clear();
    }

    public void switchLiveMapOff() {
        this.mapComponentController.toggleLiveMapOff();
        this.mapComponentController.unMarkAllStations();
        this.mapComponentController.clearAllStationsData();
    }

    public void clearMap(){
        this.mapComponentController.clearMap();
        this.mapComponentController.clearAllStationsData();
    }

    public void updateMapWithTrempRequest(TrempRequest selectedTremp) {
        this.mapComponentController.unMarkAllStations();
        this.mapComponentController.markStations(selectedTremp.getStartStation(), selectedTremp.getEndStation());

    }

    public void refreshTrempState(){
        this.trempComponentController.updateAccordingToSelectedTremp();
    }

    public void showTremps(TrempRequest trempRequest, int maxOptions) {
        List<RideForTremp> tremps =  this.logicHandler.getAllPossibleTrempsForTrempRequest(trempRequest);

        if (tremps.size() > 0) {

            tremps = tremps.stream().limit(maxOptions).collect(Collectors.toList());
            this.rideComponentController.showTremps(tremps);

        } else{
            trempComponentController.updateNoTrempsAvailable();
        }


    }

    public void assignSelectedTrempRequestToRide() {


        Task<Boolean> currentRunningTask = new assainTrempTask(
                this.trempComponentController.getSelectedTrempRequest(),
                this.rideComponentController.getSelectedTremp());

        //run the task in background thread
        new Thread(currentRunningTask).start();

        this.trempComponentController.updateTrempsList();
        this.rideComponentController.updateRidesList();
    }

    public void showRideForTremp(RideForTremp selectedRide) {
        this.mapComponentController.unMarkRedMarkedEdges();
        this.mapComponentController.unMarkBlueMarkedEdges();
        this.mapComponentController.removeTextFromEdges();


        this.mapComponentController.clearAllStationsData();
        selectedRide.getSubRides().forEach(sub -> updateMapStationsWithPartsOfRides(sub.getSelectedPartsOfRide(), null));
//        this.mapComponentController.unMarkAllStations();
        updateMapRoadsBySubRides(selectedRide.getSubRides());


//
//        markRidesStations(ridesRunningNow);
//        updateMapRoadsByRides(new ArrayList<>(){{add(selectedRide.get)}});
//        updateMapWithRideStatus(ridesRunningNow, currentDateTime);
//        updateMapRoadsTextByRides(ridesRunningNow, currentDateTime);
    }
    public void updateMapRoadsBySubRides(List<SubRide> subRides){
        boolean blue = true;
        for (SubRide subRide : subRides){
            List<Road> allRoadsToMark = subRide.getSelectedPartsOfRide().stream()
                    .map(PartOfRide::getRoad).collect(Collectors.toList());
            if (blue)
                this.mapComponentController.markRoadsInBlue(allRoadsToMark);
            else
                this.mapComponentController.markRoadsInRed(allRoadsToMark);

            hideRoads(allRoadsToMark);
            blue = !blue;
        }
        Map<Road, String> roadsWithTimes = new HashMap<>();
        subRides.forEach(subRide -> {
            subRide.getSelectedPartsOfRide().forEach(partOfRide -> {
                Road road = partOfRide.getRoad();
                LocalTime start = partOfRide.getSchedule().getStartTime();
                LocalTime end = partOfRide.getSchedule().getEndTime();
                String title = String.format("%s-%s", start, end);
                roadsWithTimes.put(road, title);
            });
        });
        this.mapComponentController.updateEdgesWithTexts(roadsWithTimes);
        subRides.forEach(subRide -> updateMapStationsWithPartsOfRides(subRide.getSelectedPartsOfRide(), null));
    }

    public void clearMapStationsData(){
        this.mapComponentController.clearAllStationsData();
    }

    public void updateMapStationsWithPartsOfRides(List<PartOfRide> allParts, LocalDateTime currentDateTIme){
        allParts.forEach(part -> mapComponentController.updateStationsWindowWithPartOfRide(part, currentDateTIme));
    }

    private void hideRoads(List<Road> roadsToMark){
        List<Road> roadsToHide = new LinkedList<>();
        List<Road> leftRoads = this.logicHandler.getAllRoads().stream().filter(rode -> !roadsToMark.contains(rode)).collect(Collectors.toList());
        for (Road toHide: leftRoads){
            for (Road toMark: roadsToMark){
                if (!roadsToMark.contains(toHide) && toHide.sharesOppositeStations(toMark))
                    roadsToHide.add(toHide);
            }
        }
        this.mapComponentController.hideRoads(roadsToHide);
    }

    public void updateMatchBtn() {
        this.trempComponentController.updateMatchText();
    }
}
