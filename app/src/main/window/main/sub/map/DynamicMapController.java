package main.window.main.sub.map;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.Model;
import com.fxgraph.graph.PannableCanvas;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.window.main.MainWindowController;
import main.window.map.component.coordinate.CoordinateNode;
import main.window.map.component.coordinate.CoordinatesManager;
import main.window.map.component.details.StationDetailsDTO;
import main.window.map.component.road.ArrowedEdge;
import main.window.map.component.station.StationManager;
import main.window.map.component.station.StationNode;
import main.window.map.layout.MapGridLayout;
import transpool.logic.map.WorldMap;
import transpool.logic.map.structure.Road;
import transpool.logic.map.structure.Station;
import transpool.logic.time.Schedule;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class DynamicMapController {

    private final int MIN_BOARD_SCALE = 10;
    private final int MAX_BOARD_SCALE = 100;
    private int currentBoardScale = 25;

    MainWindowController mainController;
    private Graph graphMap;
    private CoordinatesManager coordinatesManager;
    private StationManager stationManager;
    private List<ArrowedEdge>  allEdges;
    private List<ArrowedEdge>  markedEdges;

    Map<Road, ArrowedEdge> road2Edge;

    private String[] addSubOptions = {"Add(+)", "Sub(-)"};
    private int addSubIndex = 0;
    private String[] timeOptions = {"5 min", "30 min", "1 hour", "2 hours", "1 day"};
    private Integer[] timeMinutesOptions = {5, 30, 60, 120, 1440};
    private int timeOptionIndex = 0;
    private boolean isLiveMapOn = false;

    private LocalDateTime currentDateTime;



    @FXML
    private ScrollPane mapScrollPane;

    @FXML
    private Button zoomPlusButton;

    @FXML
    private Button zoomMinusButton;

    @FXML
    private Button liveToggleBtn;


    @FXML
    private Label dayTextField;

    @FXML
    private Button addSubBtn;

    @FXML
    private Button timeQuantityBtn;

    @FXML
    private Button applyTimeChangeBtn;

    @FXML
    private Label timeTextField;

    @FXML
    void onClickAddSubBtn(ActionEvent event) {
        updateAddSubBtn(this.addSubIndex + 1);
    }

    @FXML
    void onClickApplyTimeChangeBtn(ActionEvent event) {
        LocalDateTime newDateTime;
        if (this.addSubIndex == 0)  //add
            newDateTime =  this.currentDateTime.plusMinutes(this.timeMinutesOptions[this.timeOptionIndex]);
        else    //sub
            newDateTime = this.currentDateTime.minusMinutes(this.timeMinutesOptions[this.timeOptionIndex]);

        this.currentDateTime = newDateTime.isBefore(Schedule.minDateTime.plusDays(1)) ?
                Schedule.minDateTime.plusDays(1): newDateTime;

        updateDateTimeTextFieldsAccordingToTime(this.currentDateTime);
        this.mainController.switchLiveMapOn();
//        updateMapWithLiveTraffic();
    }

    private void updateDateTimeTextFieldsAccordingToTime(LocalDateTime dateTime){
        this.dayTextField.textProperty().set(String.format("%d", Schedule.getDayOfDateTime(dateTime)));
        this.timeTextField.textProperty().set(dateTime.toLocalTime().toString());
    }

    @FXML
    void onClickTimeQuantity(ActionEvent event) {
        updateTimeBtn(this.timeOptionIndex + 1);
    }

    @FXML
    void onClickLiveToggleBtn(ActionEvent event) {
        this.isLiveMapOn = !this.isLiveMapOn;
        applyLiveToggle();
    }

    public void applyLiveToggle(){
        if (this.isLiveMapOn){
            this.mainController.switchLiveMapOn();
//            toggleLiveMapOff();
        }
        else{
            this.mainController.switchLiveMapOff();

//            toggleLiveMapOn();
        }
    }

    public void toggleLiveMapOn() {
        this.isLiveMapOn = true;
        this.liveToggleBtn.getStyleClass().clear();
        this.liveToggleBtn.getStyleClass().add("toggle-on");
        this.liveToggleBtn.getStyleClass().add("menu-toggle");
        updateMapWithLiveTraffic();
    }

    public void toggleLiveMapOff() {
        this.isLiveMapOn = false;
        this.liveToggleBtn.getStyleClass().clear();
        this.liveToggleBtn.getStyleClass().add("toggle-of");
        this.liveToggleBtn.getStyleClass().add("menu-toggle");

        updateMapWithLiveTraffic();
    }

    private void updateMapWithLiveTraffic() {
        if (this.isLiveMapOn)
            this.mainController.updateMapWithRidesRunningOn(this.currentDateTime);
        else
            unMarkAllMarkedEdges();
    }


    @FXML
    void onZoomMinusBtnClick(ActionEvent event) {
        updateVisualMapByScale(-5);
    }

    @FXML
    void onZoomPlusBtnClick(ActionEvent event) {
        updateVisualMapByScale(5);
    }

    public void setMainController(MainWindowController mainController) {
        this.mainController = mainController;

    }

    @FXML
    public void initialize(){
        updateAddSubBtn(0);
        updateTimeBtn(0);
        this.currentDateTime = Schedule.minDateTime.plusDays(1).plusHours(8);
        updateDateTimeTextFieldsAccordingToTime(this.currentDateTime);
        toggleLiveMapOff();
    }

    private void updateAddSubBtn(int index){
        index = index % this.addSubOptions.length;
        this.addSubBtn.setText(this.addSubOptions[index]);
        this.addSubIndex = index;
    }

    private void updateTimeBtn(int index){
        index = index % this.timeOptions.length;
        this.timeQuantityBtn.setText(this.timeOptions[index]);
        this.timeOptionIndex = index;
    }



    public void initVisualMap(WorldMap worldMap) {

        this.graphMap = new Graph();
        initializeMapWithLogicMap(worldMap);

        updateVisualMapByScale(0);
    }



    private void initializeMapWithLogicMap(WorldMap worldMap){
        Model graphModel = this.graphMap.getModel();
        this.graphMap.beginUpdate();
        this.stationManager = loadStations(graphModel, worldMap.getAllStations());
        this.coordinatesManager = new CoordinatesManager(CoordinateNode::new);// createCoordinates(graphModel);
        this.allEdges = createEdges(this.coordinatesManager, worldMap.getAllRoads());
        this.allEdges.forEach(graphModel::addEdge);
        showRegularLines(this.allEdges);
        hideArrow(this.allEdges);
//        updateEdgesWithClass(this.allEdges, "road-line", "transparent-arrow-head");
//        updateEdgesUIStyle(this.allEdges);

        this.graphMap.endUpdate();
//        graph.layout(new MapGridLayout(coordinatesManager, stationManager));
    }

    private void updateVisualMapByScale(int scaleDiff){
        int newScale = currentBoardScale + scaleDiff;
        newScale = newScale < MIN_BOARD_SCALE ? MIN_BOARD_SCALE :
                Math.min(newScale, MAX_BOARD_SCALE);

        this.currentBoardScale = newScale;

        this.graphMap.layout(new MapGridLayout(coordinatesManager, stationManager, newScale));

        PannableCanvas canvas = graphMap.getCanvas();

        this.mapScrollPane.setContent(canvas);
        Platform.runLater(() -> {
            graphMap.getUseViewportGestures().set(false);
            graphMap.getUseNodeGestures().set(false);
        });
    }

//    private void updateEdgesUIStyle(List<ArrowedEdge> edges) {
//        Platform.runLater(() -> {
//            edges.forEach(edge -> {
//                edge.getLine().getStyleClass().clear();
//                edge.getLine().getStyleClass().add("road-line");
//                //edge.getText().getStyleClass().add("edge-text");
//            });
//        });
//    }

    private void updateEdgesWithSelectedStyle(List<ArrowedEdge> specialEdges) {
        if (this.markedEdges != null) {
            unMarkAllMarkedEdges();
//            updateEdgesWithClass(this.markedEdges, "road-line", "transparent-arrow-head");
            //updateEdgesUIStyle(this.allEdges);
        }
            //updateEdgesUIStyle(this.markedEdges);

        this.markedEdges = specialEdges;
        showMarkedLines(specialEdges);
        showArrow(specialEdges);
//        updateEdgesWithClass(specialEdges, "selected-road-line", "red-arrow-head");
//        Platform.runLater(() -> {
//            specialEdges.forEach(edge -> {
//                edge.getLine().getStyleClass().clear();
//                edge.getLine().getStyleClass().add("selected-road-line");
//                //edge.getText().getStyleClass().add("edge-text");    //TODO show number of trempists
//            });
//        });
    }

//    private void updateEdgesWithClass(List<ArrowedEdge> specialEdges, String lineStyleClass, String arrowStyleClass){
//        Platform.runLater(() -> {
//            specialEdges.forEach(edge -> {
//                edge.getLine().getStyleClass().clear();
//                edge.getLine().getStyleClass().add(lineStyleClass);
//                if (arrowStyleClass != null) {
//                    edge.getArrowHead().getStyleClass().clear();
//                    edge.getArrowHead().getStyleClass().add(arrowStyleClass);
//                }
//
//            });
//        });
//    }

    private StationManager loadStations(Model graphModel, List<Station> allStations) {
        StationManager stationManager = new StationManager(StationNode::new);

        allStations.forEach(station -> {
            StationNode stationNode = stationManager.getOrCreate(station.getCoordinate().getX(), station.getCoordinate().getY());
            stationNode.setName(station.getName());
            graphModel.addCell(stationNode);

            stationNode.setDetailsSupplier(() -> {
                List<String> trips = new ArrayList<>();
//                trips.add("Mosh");
//                trips.add("Menash");
//                trips.add("Tikva");
//                trips.add("Mazal");
                return new StationDetailsDTO(trips);
            });
        });

        return stationManager;
    }

    private List<ArrowedEdge> createEdges(CoordinatesManager coordinatesManager, List<Road> roads) {
        List<ArrowedEdge> edges = new LinkedList<>();

        this.road2Edge = new HashMap<>();

        roads.forEach(road -> {
            int fromX = road.getStartStation().getCoordinate().getX();
            int fromY = road.getStartStation().getCoordinate().getY();
            int toX = road.getEndStation().getCoordinate().getX();
            int toY = road.getEndStation().getCoordinate().getY();
            ArrowedEdge e = new ArrowedEdge(coordinatesManager.getOrCreate(fromX, fromY), coordinatesManager.getOrCreate(toX, toY));
//            e.textProperty().set(String.format("%s KM", road.getLengthInKM()));
            this.road2Edge.put(road, e);
            edges.add(e);


        });

        return edges;

    }

    public void updateTextOfEdge(ArrowedEdge edgeToUpdate, String setText){
        edgeToUpdate.textProperty().set(setText);
    }


    public void markRoads(List<Road> roadsToMark){
        List<ArrowedEdge>  edgesToMark = roadsToMark.stream()
                .map(rode -> this.road2Edge.get(rode))
                .collect(Collectors.toList());
        ;//createEdges(this.coordinatesManager, roadsToMark);
        updateEdgesWithSelectedStyle(edgesToMark);

    }

    public void hideRoads(List<Road> roadsToHide){
        List<ArrowedEdge>  edgesToHide = roadsToHide.stream()
                .map(rode -> this.road2Edge.get(rode))
                .collect(Collectors.toList());
//        convertLinesAndArrowsToTransparent(edgesToHide);
        hideArrow(edgesToHide);
        hideLines(edgesToHide);
    }

    public void unMarkAllMarkedEdges() {
        if (this.markedEdges != null) {
            hideArrow(this.markedEdges);
            showRegularLines(this.markedEdges);
            this.markedEdges = null;
//            convertLinesAndArrowsToTransparent(this.markedEdges);
        }
    }

    public void convertLinesAndArrowsToTransparent(List<ArrowedEdge> linesToHide){
        hideArrow(linesToHide);
        hideLines(linesToHide);
//        updateEdgesWithClass(linesToHide, "hidden-road", "transparent-arrow-head");
    }

    public void showArrow(List<ArrowedEdge> lines){
        Platform.runLater(() -> lines.forEach(line -> {
            line.getArrowHead().getStyleClass().clear();
            line.getArrowHead().getStyleClass().add("red-arrow-head");
        }));
    }

    public void hideArrow(List<ArrowedEdge> lines){
        Platform.runLater(() -> lines.forEach(line -> {
            line.getArrowHead().getStyleClass().clear();
            line.getArrowHead().getStyleClass().add("transparent-arrow-head");
        }));
    }

    public void showRegularLines(List<ArrowedEdge> lines){
        Platform.runLater(() -> lines.forEach(line -> {
            line.getLine().getStyleClass().clear();
            line.getLine().getStyleClass().add("road-line");
        }));
    }
    public void showMarkedLines(List<ArrowedEdge> lines){
        Platform.runLater(() -> lines.forEach(line -> {
            line.getLine().getStyleClass().clear();
            line.getLine().getStyleClass().add("selected-road-line");
        }));
    }

    public void hideLines(List<ArrowedEdge> lines){
        Platform.runLater(() -> lines.forEach(line -> {
            line.getLine().getStyleClass().clear();
            line.getLine().getStyleClass().add("hidden-road");
        }));
    }
}
/*
* updateEdgesWithClass(this.markedEdges, "road-line", "transparent-arrow-head");*/
