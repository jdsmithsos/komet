/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.amplify.lidr.details;

import dev.ikm.komet.amplify.lidr.events.AddDeviceEvent;
import dev.ikm.komet.amplify.lidr.events.AddResultInterpretationEvent;
import dev.ikm.komet.amplify.lidr.events.LidrPropertyPanelEvent;
import dev.ikm.komet.amplify.lidr.events.ShowPanelEvent;
import dev.ikm.komet.amplify.lidr.om.DataModelHelper;
import dev.ikm.komet.amplify.lidr.om.LidrRecord;
import dev.ikm.komet.amplify.lidr.properties.PropertiesController;
import dev.ikm.komet.amplify.lidr.viewmodels.AnalyteGroupViewModel;
import dev.ikm.komet.amplify.lidr.viewmodels.LidrViewModel;
import dev.ikm.komet.amplify.mvvm.ValidationViewModel;
import dev.ikm.komet.amplify.mvvm.ViewModel;
import dev.ikm.komet.amplify.mvvm.loader.*;
import dev.ikm.komet.amplify.om.DescrName;
import dev.ikm.komet.amplify.stamp.StampEditController;
import dev.ikm.komet.amplify.timeline.TimelineController;
import dev.ikm.komet.amplify.viewmodels.StampViewModel;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.provider.search.Searcher;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

import static dev.ikm.komet.amplify.commons.CssHelper.defaultStyleSheet;
import static dev.ikm.komet.amplify.commons.SlideOutTrayHelper.*;
import static dev.ikm.komet.amplify.commons.ViewportHelper.clipChildren;
import static dev.ikm.komet.amplify.lidr.details.LidrRecordDetailsController.LIDR_RECORD_FXML;
import static dev.ikm.komet.amplify.lidr.events.LidrPropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.amplify.lidr.events.LidrPropertyPanelEvent.OPEN_PANEL;
import static dev.ikm.komet.amplify.lidr.events.ShowPanelEvent.SHOW_ADD_ANALYTE_GROUP;
import static dev.ikm.komet.amplify.lidr.events.ShowPanelEvent.SHOW_ADD_DEVICE;
import static dev.ikm.komet.amplify.lidr.om.DataModelHelper.findDeviceManufacturer;
import static dev.ikm.komet.amplify.lidr.viewmodels.AnalyteGroupViewModel.LIDR_RECORD;
import static dev.ikm.komet.amplify.lidr.viewmodels.LidrViewModel.CONCEPT_TOPIC;
import static dev.ikm.komet.amplify.lidr.viewmodels.LidrViewModel.CREATE;
import static dev.ikm.komet.amplify.lidr.viewmodels.LidrViewModel.EDIT;
import static dev.ikm.komet.amplify.lidr.viewmodels.LidrViewModel.VIEW;
import static dev.ikm.komet.amplify.lidr.viewmodels.LidrViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.amplify.lidr.viewmodels.LidrViewModel.*;
import static dev.ikm.komet.amplify.lidr.viewmodels.ViewModelHelper.addNewLidrRecord;
import static dev.ikm.komet.amplify.viewmodels.FormViewModel.MODE;
import static dev.ikm.komet.amplify.viewmodels.StampViewModel.*;

public class LidrDetailsController {
    private static final Logger LOG = LoggerFactory.getLogger(LidrDetailsController.class);
    public static final String EDIT_STAMP_OPTIONS_FXML = "edit-stamp.fxml";
    public static final String LIDR_PROPERTIES_VIEW_FXML_FILE = "lidr-properties.fxml";
    protected static final String CONCEPT_TIMELINE_VIEW_FXML_FILE = "amplify-timeline.fxml";

    @FXML
    private Button closeConceptButton;

    /**
     * The outermost part of the details (may remove)
     */
    @FXML
    private BorderPane detailsOuterBorderPane;

    /**
     * The inner border pane contains all content.
     */
    @FXML
    private BorderPane detailsCenterBorderPane;

    @FXML
    private Button addDescriptionButton;


    //////////  Banner area /////////////////////
    @FXML
    private ImageView identiconImageView;

    @FXML
    private Label deviceTitleText;
    @FXML
    private Tooltip conceptNameTooltip;


    @FXML
    private TextField snomedIdentifierText;

    @FXML
    private TextField identifierText;

    @FXML
    private Tooltip identifierTooltip;

    @FXML
    private Text lastUpdatedText;

    @FXML
    private Text moduleText;

    @FXML
    private Text pathText;

    @FXML
    private Text originationText;

    @FXML
    private Text statusText;

    /**
     * Applied to lastUpdatedText component.
     */
    private Tooltip authorTooltip = new Tooltip();

    ///// Descriptions Section /////////////////////////////////
    @FXML
    private TitledPane descriptionsTitledPane;
    @FXML
    private Button editConceptButton;

    @FXML
    private Text deviceSummaryText;

    @FXML
    private Text mfgSummaryText;

    ///// Results Section    ///////////////////
    @FXML
    private Button addAnalyteGroupButton; // see action showAnalyteGroupPanel() method.

    @FXML
    private TitledPane lidrRecordDetailsTitledPane;
    @FXML
    private VBox lidrRecordsVBox;

    @FXML
    private Button elppSemanticCountButton;

    @FXML
    private HBox conceptHeaderControlToolBarHbox;

    /**
     * Opens or slides out the properties window.
     */
    @FXML
    private ToggleButton propertiesToggleButton;
    /**
     * This is called after dependency injection has occurred to the JavaFX controls above.
     */

    /**
     * Used slide out the properties view
     */
    @FXML
    private Pane propertiesSlideoutTrayPane;

    @FXML
    private Pane timelineSlideoutTrayPane;

    /**
     * A function from the caller. This class passes a boolean true if classifier button is pressed invoke caller's function to be returned a controller.
     */
    private Consumer<ToggleButton> reasonerResultsControllerConsumer;

    @InjectViewModel
    private LidrViewModel lidrViewModel;
    private EvtBus eventBus;
    
    private UUID conceptTopic;

    /**
     * Stamp Edit
     */
    private PopOver stampEdit;
    private StampEditController stampEditController;

    private PropertiesController propertiesViewController;
    private BorderPane propertiesViewBorderPane;
    private BorderPane timelineViewBorderPane;
    private TimelineController timelineViewController;
    // style the same as the details view

    public LidrDetailsController() {
    }

    @FXML
    public void initialize() {
        // event bus will listen on this topic.
        if (conceptTopic == null) {
            // if not set caller used the one set inside the view model.
            conceptTopic = lidrViewModel.getPropertyValue(CONCEPT_TOPIC);
        }

        Tooltip.install(identifierText, identifierTooltip);
        Tooltip.install(snomedIdentifierText, identifierTooltip);
        Tooltip.install(lastUpdatedText, authorTooltip);

        clearView();

        eventBus = EvtBusFactory.getDefaultEvtBus();

        // listen for open and close events
        Subscriber<LidrPropertyPanelEvent> propBumpOutListener = (evt) -> {
                if (evt.getEventType() == CLOSE_PANEL) {
                    LOG.info("propBumpOutListener - Close Properties bumpout toggle = " + propertiesToggleButton.isSelected());
                    propertiesToggleButton.setSelected(false);
                    if (isOpen(propertiesSlideoutTrayPane)) {
                        slideIn(propertiesSlideoutTrayPane, detailsOuterBorderPane);
                    }
                } else if (evt.getEventType() == OPEN_PANEL) {
                    LOG.info("propBumpOutListener - Opening Properties bumpout toggle = " + propertiesToggleButton.isSelected());
                    propertiesToggleButton.setSelected(true);
                    if (isClosed(propertiesSlideoutTrayPane)) {
                        slideOut(propertiesSlideoutTrayPane, detailsOuterBorderPane);
                    }
                }
        };
        eventBus.subscribe(conceptTopic, LidrPropertyPanelEvent.class, propBumpOutListener);

        // Listen when a new device is being added to this lidr details
        Subscriber<AddDeviceEvent> addDeviceEventSubscriber = (evt) -> {
            // TODO Update the UI and add device.
            LOG.info("addDeviceEventSubscriber -> TODO Update the UI and add a new device.");
            EntityFacade currentDevice = getLidrViewModel().getPropertyValue(DEVICE_ENTITY);
            boolean sameDevice = currentDevice == null ? false : PublicId.equals(evt.deviceEntity.publicId(), currentDevice.publicId());
            // if it's a different device than clear details and
            if (sameDevice) {
                return;
            }
            getLidrViewModel().setPropertyValue(DEVICE_ENTITY, evt.deviceEntity);
            clearView();
            updateView();
        };
        eventBus.subscribe(conceptTopic, AddDeviceEvent.class, addDeviceEventSubscriber);

        // Listen when a new analyte group is added to this device. Will write to db and add to Lidr record details
        Subscriber<AddResultInterpretationEvent> addResultInterpretationEventSubscriber = (evt) -> {

            LOG.info("addResultInterpretationEventSubscriber -> Lidr created and details displayed");
            LidrRecord lidrRecord = evt.getLidrRecord();
            // Create a lidr record in the database.
            Concept device = getLidrViewModel().getPropertyValue(DEVICE_ENTITY);

            // TODO Database will need to have the following targets, and resultsDataType:
            PublicId targetMatrixM1Id = PublicIds.of("1d9ab589-2fd1-331e-a79d-e9190c415d36");
            PublicId resultOrdinalId = PublicIds.of("3bf24a2e-7c1d-3cad-84e9-bdda58df5905");

            PublicId testPerformedId = lidrRecord.testPerformedId() == null ? targetMatrixM1Id : lidrRecord.testPerformedId();
            LidrRecord newLidrRecord = new LidrRecord(
                    lidrRecord.lidrRecordId(),
                    testPerformedId,  /* todo use dummy data */
                    resultOrdinalId,  /* todo use dummy data */
                    lidrRecord.analyte(),
                    lidrRecord.targets(),
                    lidrRecord.specimens(),
                    lidrRecord.resultConformances());
            PublicId lidrPublicId = addNewLidrRecord(newLidrRecord, device.publicId(), getStampViewModel());

            // Populate with the Accordion containing one Analyte Group (aka LIDR record semantic record)
            addLidrRecordDetailsAccordion(lidrPublicId);
        };
        eventBus.subscribe(conceptTopic, AddResultInterpretationEvent.class, addResultInterpretationEventSubscriber);

        // Setup Properties
        setupProperties();
        setupTimelineBumpOut();
    }

    /**
     * Adds an accordion of a lidr record details.
     * @param lidrRecordPublicId public id of existing lidr record details
     */
    private void addLidrRecordDetailsAccordion(PublicId lidrRecordPublicId) {
        LidrRecord lidrRecord = null;
        try {
            lidrRecord = DataModelHelper.makeLidrRecord(lidrRecordPublicId);
        } catch (NoSuchElementException ex) {
            // TODO data is bad. Not able to get Stated or Inferred discription logic in axioms.
            LOG.error("Error Not able to get Stated or Inferred description logic in axioms.", ex);
            return; // eat exception
        }
        // Populate with the Accordion containing one Analyte Group (aka LIDR record semantic record)
        AnalyteGroupViewModel analyteGroupViewModel = new AnalyteGroupViewModel();
        analyteGroupViewModel.setPropertyValue(CONCEPT_TOPIC, conceptTopic)
                .addProperty(LIDR_RECORD, lidrRecord)
                .save(true);
        NamedVm analyteViewModel = new NamedVm("analyteGroupViewModel", analyteGroupViewModel);
        JFXNode<TilePane, LidrRecordDetailsController> lidrNodeController = FXMLMvvmLoader.make(this.getClass().getResource(LIDR_RECORD_FXML), analyteViewModel);
        Platform.runLater(()-> {
            lidrRecordsVBox.getChildren().add(lidrNodeController.node());
            lidrNodeController.controller().updateView();
        });
    }
    private void setupProperties() {
        // Setup Property screen bump out
        // Load Concept Properties View Panel (FXML & Controller)
        JFXNode<BorderPane, PropertiesController> propsFXMLLoader = FXMLMvvmLoader.make(PropertiesController.class.getResource(LIDR_PROPERTIES_VIEW_FXML_FILE),
                new PropertiesController(conceptTopic));
        this.propertiesViewBorderPane = propsFXMLLoader.node();
        this.propertiesViewController = propsFXMLLoader.controller();
        // style the same as the details view
        String styleSheet = defaultStyleSheet();
        this.propertiesViewBorderPane.getStylesheets().add(styleSheet);
        this.propertiesViewController.updateModel(getViewProperties(), null);

        // setup view and controller into details controller
        attachPropertiesViewSlideoutTray(this.propertiesViewBorderPane);
    }

    private void setupTimelineBumpOut() {
        // Load Timeline View Panel (FXML & Controller)
        FXMLLoader timelineFXMLLoader = new FXMLLoader(TimelineController.class.getResource(CONCEPT_TIMELINE_VIEW_FXML_FILE));
        try {
            this.timelineViewBorderPane = timelineFXMLLoader.load();
            this.timelineViewController = timelineFXMLLoader.getController();

            // This will highlight with green around the pane when the user selects a date point in the timeline.
            timelineViewController.onDatePointSelected((changeCoordinate) ->{
                propertiesViewController.getHistoryChangeController().highlightListItemByChangeCoordinate(changeCoordinate);
            });
            // When Date points are in range (range slider)
            timelineViewController.onDatePointInRange((rangeToggleOn, changeCoordinates) -> {
                if (rangeToggleOn) {
                    propertiesViewController.getHistoryChangeController().filterByRange(changeCoordinates);
                    propertiesViewController.getHierarchyController().diffNavigationGraph(changeCoordinates);
                } else {
                    propertiesViewController.getHistoryChangeController().unfilterByRange();
                    propertiesViewController.getHierarchyController().diffNavigationGraph(Set.of());
                }
            });

            // style the same as the details view
            this.timelineViewBorderPane.getStylesheets().add(defaultStyleSheet());

            // setup view and controller into details controller
            attachTimelineViewSlideoutTray(this.timelineViewBorderPane);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public ViewProperties getViewProperties() {
        return getLidrViewModel().getPropertyValue(VIEW_PROPERTIES);
    }

    public ValidationViewModel getLidrViewModel() {
        return lidrViewModel;
    }

    public ValidationViewModel getStampViewModel() {
        return lidrViewModel.getPropertyValue(STAMP_VIEW_MODEL);
    }

    @FXML
    private void showAddAnalyteGroupPanel(ActionEvent actionEvent) {
        // Todo show bump out and display Add analyte group panel.
        System.out.println("Todo show bump out and display Add analyte group panel \n" + actionEvent);
        // publish show Add analyte group panel
        eventBus.publish(conceptTopic, new ShowPanelEvent(actionEvent.getSource(), SHOW_ADD_ANALYTE_GROUP));
        // publish property open.
        eventBus.publish(conceptTopic, new LidrPropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));

    }
    @FXML
    private void showAddDevicePanel(ActionEvent actionEvent) {
        // Todo show bump out and display Add Device and MFG panel
        System.out.println("Todo show bump out and display Add Device and MFG panel \n" + actionEvent);
        // publish show Add analyte group panel
        eventBus.publish(conceptTopic, new ShowPanelEvent(actionEvent.getSource(), SHOW_ADD_DEVICE));
        // publish property open.
        eventBus.publish(conceptTopic, new LidrPropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));
    }


    public void attachPropertiesViewSlideoutTray(Pane propertiesViewBorderPane) {
        addPaneToTray(propertiesViewBorderPane, propertiesSlideoutTrayPane);
    }
    public void attachTimelineViewSlideoutTray(Pane timelineViewBorderPane) {
        addPaneToTray(timelineViewBorderPane, timelineSlideoutTrayPane);
    }
    private void addPaneToTray(Pane contentViewPane, Pane slideoutTrayPane) {
        double width = contentViewPane.getWidth();
        contentViewPane.setLayoutX(width);
        contentViewPane.getStyleClass().add("slideout-tray-pane");

        slideoutTrayPane.getChildren().add(contentViewPane);
        clipChildren(slideoutTrayPane, 0);
        contentViewPane.setLayoutX(-width);
        slideoutTrayPane.setMaxWidth(0);

        Region contentRegion = contentViewPane;
        // binding the child's height to the preferred height of hte parent
        // so that when we resize the window the content in the slide out pane
        // aligns with the details controller
        contentRegion.prefHeightProperty().bind(slideoutTrayPane.heightProperty());
    }
    private Consumer<LidrDetailsController> onCloseConceptWindow;
    public void setOnCloseConceptWindow(Consumer<LidrDetailsController> onClose) {
        this.onCloseConceptWindow = onClose;
    }
    @FXML
    void closeConceptWindow(ActionEvent event) {
        LOG.info("Cleanup occurring: Closing Window with concept: " + deviceTitleText.getText());
        if (this.onCloseConceptWindow != null) {
            onCloseConceptWindow.accept(this);
        }
        Pane parent = (Pane) detailsOuterBorderPane.getParent();
        parent.getChildren().remove(detailsOuterBorderPane);
    }
    public Pane getPropertiesSlideoutTrayPane() {
        return propertiesSlideoutTrayPane;
    }

    public void updateView() {
        EntityFacade entityFacade = lidrViewModel.getPropertyValue(DEVICE_ENTITY);
        if (entityFacade != null) {
            EntityVersion latestVersion = getViewProperties().calculator().latest(entityFacade).get();
            StampEntity stamp = latestVersion.stamp();

            getLidrViewModel().setPropertyValue(MODE, EDIT);
            if (getLidrViewModel().getPropertyValue(STAMP_VIEW_MODEL) == null) {

                // add a new stamp view model to the concept view model
                StampViewModel stampViewModel = new StampViewModel();
                stampViewModel.setPropertyValue(MODE, EDIT)
                        .setPropertyValue(STATUS_PROPERTY, stamp.state())
                        .setPropertyValue(AUTHOR_PROPERTY, stamp.author())
                        .setPropertyValue(MODULE_PROPERTY, stamp.module())
                        .setPropertyValue(PATH_PROPERTY, stamp.path())
                        .setPropertyValues(MODULES_PROPERTY, stampViewModel.findAllModules(getViewProperties()), true)
                        .setPropertyValues(PATHS_PROPERTY, stampViewModel.findAllPaths(getViewProperties()), true);

                getLidrViewModel().setPropertyValue(STAMP_VIEW_MODEL,stampViewModel);
            }

            // TODO: Ability to change Concept record. but for now user can edit stamp but not affect Concept version.
            updateStampViewModel(EDIT, stamp);
        }

        // Display info for top banner area
        updateDeviceBanner();

        // Display Description info area
        updateDeviceAndMfg();

        // Update Lidr Record Details
        refreshLidrRecordDetails();

    }
    public void onReasonerSlideoutTray(Consumer<ToggleButton> reasonerResultsControllerConsumer) {
        this.reasonerResultsControllerConsumer = reasonerResultsControllerConsumer;
    }
    public void updateDeviceBanner(DescrName fqnDescrName) {
        if (fqnDescrName == null) return;

        // Title (FQN of concept)
        String conceptNameStr = fqnDescrName.nameText();
        deviceTitleText.setText(conceptNameStr);
        conceptNameTooltip.setText(conceptNameStr);

    }
    /**
     * Responsible for populating the top banner area of the concept view panel.
     */
    public void updateDeviceBanner() {
        EntityFacade entityFacade = getLidrViewModel().getPropertyValue(DEVICE_ENTITY);
        if (entityFacade == null) {
            identiconImageView.setImage(null);
            snomedIdentifierText.setText("");
            identifierText.setText("");
            deviceSummaryText.setText("");
            conceptNameTooltip.setText("");
            deviceTitleText.setText("");
            return;
        }

        deviceTitleText.setText(entityFacade.description());
        conceptNameTooltip.setText(entityFacade.description());

        // TODO do a null check on the entityFacade
        final ViewCalculator viewCalculator = getViewProperties().calculator();

        // Public ID (UUID)
        String uuidStr = entityFacade.publicId() != null ? entityFacade.publicId().asUuidArray()[0].toString(): "";
        identifierText.setText(uuidStr);
        identifierTooltip.setText(uuidStr);

        // Identicon
        Image identicon = Identicon.generateIdenticonImage(entityFacade.publicId());
        identiconImageView.setImage(identicon);
        String formMode = getLidrViewModel().getPropertyValue(MODE);
        if (VIEW.equals(formMode) || EDIT.equals(formMode)) {
            // Obtain STAMP info
            EntityVersion latestVersion = viewCalculator.latest(entityFacade).get();
            StampEntity stamp = latestVersion.stamp();

            // Status
            String status = stamp.state() != null && State.ACTIVE == stamp.state() ? "Active" : "Inactive";
            statusText.setText(status);

            // Module
            String module = stamp.module().description();
            moduleText.setText(module);

            // Path
            String path = stamp.path().description();
            pathText.setText(path);

            // Latest update time
            DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss");
            Instant stampInstance = Instant.ofEpochSecond(stamp.time() / 1000);
            ZonedDateTime stampTime = ZonedDateTime.ofInstant(stampInstance, ZoneOffset.UTC);
            String time = DATE_TIME_FORMATTER.format(stampTime);
            lastUpdatedText.setText(time);

            // Author tooltip
            authorTooltip.setText(stamp.author().description());

            updateStampViewModel(EDIT, stamp);
        }

    }

    private void updateStampViewModel(String mode, StampEntity stamp) {
        ValidationViewModel stampViewModel = getLidrViewModel().getPropertyValue(STAMP_VIEW_MODEL);
        if (getLidrViewModel().getPropertyValue(STAMP_VIEW_MODEL) != null) {
            stampViewModel.setPropertyValue(MODE, mode)
                    .setPropertyValue(STATUS_PROPERTY, stamp.state())
                    .setPropertyValue(MODULE_PROPERTY, stamp.module())
                    .setPropertyValue(PATH_PROPERTY, stamp.path())
                    .setPropertyValue(TIME_PROPERTY, stamp.time())
                    .save(true);
        }
    }

    /**
     * Responsible for populating the Descriptions TitledPane area. This retrieves the latest concept version and
     * semantics for language and case significance.
     */
    public void updateDeviceAndMfg() {
        // do not update ui should be blank
        EntityFacade entityFacade = getLidrViewModel().getPropertyValue(DEVICE_ENTITY);
        if (entityFacade == null) {
            deviceSummaryText.setText("");
            mfgSummaryText.setText("");
            return;
        }

        // Display the name of the device
        deviceSummaryText.setText(entityFacade.description());

        // Update manufacturer if one exists
        Optional<Concept> mfg = findDeviceManufacturer(entityFacade.publicId());
        mfg.ifPresentOrElse(concept -> mfgSummaryText.setText(
                ((ConceptFacade) concept).description()),
                ()-> mfgSummaryText.setText("")
        );
    }

    /**
     * Refresh the Lidr Record Details (Accordions). Clears VBox and populates each lidr record.
     */
    private void refreshLidrRecordDetails() {
        // do not update ui should be blank
        if (getLidrViewModel().getPropertyValue(MODE) == CREATE) {
            return;
        }
        // populate the lidr record details
        EntityFacade entityFacade = getLidrViewModel().getPropertyValue(DEVICE_ENTITY);
        List<PublicId> lidrRecordIds = Searcher.getLidrRecordSemanticsFromTestKit(entityFacade.publicId());
        lidrRecordIds.forEach(lidrRecordPublicId -> {
            // Populate with the Accordion containing one Analyte Group (aka LIDR record semantic record)
            addLidrRecordDetailsAccordion(lidrRecordPublicId);
        });
    }


    public void clearView() {
        identiconImageView.setImage(null);
        snomedIdentifierText.setText("");
        identifierText.setText("");
        lastUpdatedText.setText("");
        moduleText.setText("");
        pathText.setText("");
        originationText.setText("");
        statusText.setText("");
        authorTooltip.setText("");
        lidrRecordsVBox.getChildren().clear();
    }

    @FXML
    private void openPropertiesPanel(ActionEvent event) {
        ToggleButton propertyToggle = (ToggleButton) event.getSource();
        EvtType<LidrPropertyPanelEvent> eventEvtType = propertyToggle.isSelected() ? OPEN_PANEL : CLOSE_PANEL;
        eventBus.publish(conceptTopic, new LidrPropertyPanelEvent(propertyToggle, eventEvtType));
    }

    @FXML
    private void openTimelinePanel(ActionEvent event) {
        ToggleButton timelineToggle = (ToggleButton) event.getSource();
        // if selected open properties
        if (timelineToggle.isSelected()) {
            LOG.info("Opening slideout of timeline panel");
            slideOut(timelineSlideoutTrayPane, detailsOuterBorderPane);
        } else {
            LOG.info("Close Properties timeline panel");
            slideIn(timelineSlideoutTrayPane, detailsOuterBorderPane);
        }
    }

    @FXML
    private void openReasonerSlideout(ActionEvent event) {
        ToggleButton reasonerToggle = (ToggleButton) event.getSource();
        reasonerResultsControllerConsumer.accept(reasonerToggle);
    }

    /**
     * When user selects the STAMP edit button to pop up the options to edit.
     * @param event
     */
    @FXML
    public void popupStampEdit(ActionEvent event) {
        if (stampEdit !=null && stampEditController != null) {
            stampEdit.show((Node) event.getSource());
            return;
        }

        // Inject Stamp view model into form.
        Config stampConfig = new Config()
                .fxml(StampEditController.class.getResource(EDIT_STAMP_OPTIONS_FXML))
                .addNamedViewModel(new NamedVm("stampViewModel", lidrViewModel.getPropertyValue(STAMP_VIEW_MODEL)));
        JFXNode<Pane, StampEditController> stampJFXNode = FXMLMvvmLoader.make(stampConfig);

        Pane editStampPane = stampJFXNode.node();
        PopOver popOver = new PopOver(editStampPane);
        popOver.getStyleClass().add("filter-menu-popup");
        StampEditController stampEditController = stampJFXNode.controller();

        stampEditController.updateModel(getViewProperties());
        popOver.setOnHidden(windowEvent -> {
            // set Stamp info into Details form
            getStampViewModel().save();
            updateUIStamp(getStampViewModel());
        });
        popOver.show((Node) event.getSource());

        // store and use later.
        stampEdit = popOver;
        this.stampEditController = stampEditController;
    }

    private void updateUIStamp(ViewModel stampViewModel) {
        long time = stampViewModel.getValue(TIME_PROPERTY);
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss");
        Instant stampInstance = Instant.ofEpochSecond(time/1000);
        ZonedDateTime stampTime = ZonedDateTime.ofInstant(stampInstance, ZoneOffset.UTC);
        String lastUpdated = DATE_TIME_FORMATTER.format(stampTime);

        lastUpdatedText.setText(lastUpdated);
        ConceptEntity moduleEntity = stampViewModel.getValue(MODULE_PROPERTY);
        if (moduleEntity == null) {
            LOG.warn("Must select a valid module for Stamp.");
            return;
        }
        String moduleDescr = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(moduleEntity.nid());
        moduleText.setText(moduleDescr);
        ConceptEntity pathEntity = stampViewModel.getValue(PATH_PROPERTY);
        String pathDescr = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(pathEntity.nid());
        pathText.setText(pathDescr);
        statusText.setText(stampViewModel.getValue(STATUS_PROPERTY).toString());
    }

    public void compactSizeWindow() {
        descriptionsTitledPane.setExpanded(false);
        lidrRecordDetailsTitledPane.setExpanded(false);
        //581 x 242
        detailsOuterBorderPane.setPrefSize(581, 242);
    }

    public void setConceptTopic(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }
}
