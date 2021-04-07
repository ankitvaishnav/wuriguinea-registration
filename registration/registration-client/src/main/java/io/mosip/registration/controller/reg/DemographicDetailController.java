package io.mosip.registration.controller.reg;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.mosip.registration.entity.Registration;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;

import io.mosip.kernel.core.applicanttype.exception.InvalidApplicantArgumentException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.PridValidator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.transliteration.spi.Transliteration;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.packetmanager.dto.SimpleDto;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.AuditEvent;
import io.mosip.registration.constants.AuditReferenceIdTypes;
import io.mosip.registration.constants.Components;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.constants.RegistrationUIConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.controller.BaseController;
import io.mosip.registration.controller.FXUtils;
import io.mosip.registration.controller.VirtualKeyboard;
import io.mosip.registration.controller.device.BiometricsController;
import io.mosip.registration.dao.MasterSyncDao;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.dto.UiSchemaDTO;
import io.mosip.registration.dto.mastersync.GenericDto;
import io.mosip.registration.dto.mastersync.LocationDto;
import io.mosip.registration.entity.Location;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.IdentitySchemaService;
import io.mosip.registration.service.sync.MasterSyncService;
import io.mosip.registration.service.sync.PreRegistrationDataSyncService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleBooleanProperty;
import javax.swing.*;

/**
 * {@code DemographicDetailController} is to capture the demographic details
 *
 * @author Taleev.Aalam
 * @since 1.0.0
 *
 */

@Controller
public class DemographicDetailController extends BaseController {

	/**
	 * Instance of {@link Logger}
	 */
	private static final Logger LOGGER = AppConfig.getLogger(DemographicDetailController.class);

	@FXML
	public TextField preRegistrationId;

	@FXML
	private GridPane parentDetailPane;
	@FXML
	private ScrollPane parentScrollPane;
	private boolean isChild;
	private Node keyboardNode;
	@Autowired
	private PridValidator<String> pridValidatorImpl;

	@Autowired
	private Validations validation;
	@Autowired
	private MasterSyncService masterSync;
	@FXML
	private FlowPane parentFlowPane;
	@FXML
    private FlowPane childFlowPanePre;
    @FXML
    private FlowPane childFlowPaneInfo;
    @FXML
    private FlowPane childFlowPaneGuardian;
    @FXML
    private FlowPane childFlowPaneAddress;
    @FXML
	private GridPane scrollParentPane;
	@FXML
	private GridPane preRegParentPane;
	@Autowired
	private DateValidation dateValidation;
	@Autowired
	private RegistrationController registrationController;
	@Autowired
	private DocumentScanController documentScanController;
	@Autowired
	private Transliteration<String> transliteration;
	@Autowired
	private PreRegistrationDataSyncService preRegistrationDataSyncService;

	private FXUtils fxUtils;
	private int minAge;
	private int maxAge;

	@FXML
	private HBox parentDetailsHbox;
	@FXML
	private HBox localParentDetailsHbox;
	@FXML
	private AnchorPane ridOrUinToggle;

	@Autowired
	private MasterSyncService masterSyncService;
	@FXML
	private GridPane borderToDo;
	@FXML
	private Label registrationNavlabel;
	@FXML
	private AnchorPane keyboardPane;
	@FXML
	private Button continueBtn;
    @FXML
    private AnchorPane personalInfoPane;
	private boolean lostUIN = false;
	private ResourceBundle applicationLabelBundle;
	private ResourceBundle localLabelBundle;
	private String primaryLanguage;
	private String secondaryLanguage;
	private List<String> orderOfAddress;
	boolean hasToBeTransliterated = true;
	// public Map<String, ComboBox<String>> listOfComboBoxWithString;
	public Map<String, ComboBox<GenericDto>> listOfComboBoxWithObject;
	public Map<String, TextField> listOfTextField;
	private int age = 0;
	@Autowired
	private IdentitySchemaService identitySchemaService;
	@Autowired
	private MasterSyncDao masterSyncDao;
	private VirtualKeyboard vk;
	private HashMap<String, Integer> positionTracker;
	int lastPosition;
	private ObservableList<Node> parentFlow;
    private ObservableList<Node> childFlow;
    private ObservableList<Node> childFlowAddress;
    private ObservableList<Node> childFlowParent;
	private boolean keyboardVisible = false;

	@Autowired
	private BiometricsController guardianBiometricsController;
	Button tsButton = new Button();
	Label tsLabel= new Label();
	HBox tsHBox = new HBox();
	SimpleBooleanProperty switchedOn = new SimpleBooleanProperty(true);
	public SimpleBooleanProperty switchOnProperty()
	{
		return switchedOn;
	}

	/*Additional address limit to 40 */
	public static void addTextLimiter(final TextField tf, final int maxLength) {
		tf.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
				if (tf.getText().length() > maxLength) {
					String s = tf.getText().substring(0, maxLength);
					tf.setText(s);
				}
			}
		});
	}
	/*Toggle button initilisation */
    private void iniToggleButton()
	{
		tsHBox.getChildren().clear();
		tsHBox.getChildren().addAll(tsButton, tsLabel);
		parentFlowPane.lookup(RegistrationConstants.HASH.concat("parentOrGuardianUIN")).setDisable(true);
		switchedOn.addListener((a, b, c) -> {
			if (c) {
				tsLabel.setText("");
				tsLabel.toFront();
				parentFlowPane.lookup(RegistrationConstants.HASH.concat("parentOrGuardianUIN")).setDisable(true);
				parentFlowPane.lookup(RegistrationConstants.HASH.concat("parentOrGuardianRID")).setDisable(false);
			} else {
				tsLabel.setText("");
				tsButton.toFront();
				parentFlowPane.lookup(RegistrationConstants.HASH.concat("parentOrGuardianUIN")).setDisable(false);
				parentFlowPane.lookup(RegistrationConstants.HASH.concat("parentOrGuardianRID")).setDisable(true);
			}});

		tsButton.setOnAction((e) -> {
			switchedOn.set(!switchedOn.get());
		});
		tsLabel.setOnMouseClicked((e) -> {
			switchedOn.set(!switchedOn.get());
		});
		bindProperties();
	}

	/* Style for the toogleButton */
	private void setStyle() {
		tsHBox.setMaxWidth(35);
		tsHBox.setMaxHeight(10);
		tsHBox.setAlignment(Pos.CENTER);
		tsButton.setStyle("-fx-background-color: #020F59; -fx-border-width: 0.0;-fx-text-fill:#020F59; -fx-background-radius: 5em; -fx-font-size: 1em;" +
				"-fx-min-width: 13px; -fx-min-height: 13px; -fx-max-width: 13px; -fx-max-height: 13px;");
		tsHBox.setStyle("-fx-background-color: white; -fx-border-color:#020F59; -fx-border-radius:45px; -fx-border-width: 1;");
	}

	/*Binding the composant for toggle buttion together*/
	private void bindProperties() {
		tsLabel.prefWidthProperty().bind(tsHBox.widthProperty().divide(2));
		tsLabel.prefHeightProperty().bind(tsHBox.heightProperty());
		tsButton.prefWidthProperty().bind(tsHBox.widthProperty().divide(2));
		tsButton.prefHeightProperty().bind(tsHBox.heightProperty());
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see javafx.fxml.Initializable#initialize()
	 */

	@FXML
	private void initialize() throws IOException {

		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Entering the Demographic Details Screen");

		// listOfComboBoxWithString = new HashMap<>();
		listOfComboBoxWithObject = new HashMap<>();
		listOfTextField = new HashMap<>();
		lastPosition = -1;
		positionTracker = new HashMap<>();
		fillOrderOfLocation();
		vk = VirtualKeyboard.getInstance();
		keyboardNode = vk.view();
		if (ApplicationContext.getInstance().getApplicationLanguage()
				.equals(ApplicationContext.getInstance().getLocalLanguage())) {
			hasToBeTransliterated = false;
		}

		try {
			applicationLabelBundle = applicationContext.getApplicationLanguageBundle();
			if (getRegistrationDTOFromSession() == null) {
				validation.updateAsLostUIN(false);
				registrationController.createRegistrationDTOObject(RegistrationConstants.PACKET_TYPE_NEW);
			}

			if (validation.isLostUIN()) {
				registrationNavlabel.setText(applicationLabelBundle.getString("/lostuin"));
				disablePreRegFetch();
			}

			if (getRegistrationDTOFromSession() != null
					&& getRegistrationDTOFromSession().getSelectionListDTO() == null) {
				getRegistrationDTOFromSession().setUpdateUINNonBiometric(false);
				getRegistrationDTOFromSession().setUpdateUINChild(false);
			}
			validation.setChild(false);
			lostUIN = false;
			fxUtils = FXUtils.getInstance();
			fxUtils.setTransliteration(transliteration);
			isChild = false;
			minAge = Integer.parseInt(getValueFromApplicationContext(RegistrationConstants.MIN_AGE));
			maxAge = Integer.parseInt(getValueFromApplicationContext(RegistrationConstants.MAX_AGE));
			localLabelBundle = applicationContext.getLocalLanguageProperty();
			primaryLanguage = applicationContext.getApplicationLanguage();
			secondaryLanguage = applicationContext.getLocalLanguage();
			parentFlow = parentFlowPane.getChildren();
            childFlow = childFlowPaneInfo.getChildren();
            childFlowAddress = childFlowPaneAddress.getChildren();
            childFlowParent = childFlowPaneGuardian.getChildren();

			int position = parentFlow.size() - 1;
            int position1 = childFlow.size() - 1;
            int position2 = childFlow.size() - 1;
            int position3 = childFlow.size() - 1;
            /* layout category */
			ArrayList<String> infoFields = new ArrayList<String>();
			infoFields.add("firstName");
			infoFields.add("lastName");
			infoFields.add("gender");
			infoFields.add("dateOfBirth");
			infoFields.add("phone");
			infoFields.add("email");
			infoFields.add("residenceStatus");

			ArrayList<String> addressFields = new ArrayList<String>();
			addressFields.add("region");
			addressFields.add("prefecture");
			addressFields.add("subPrefectureOrCommune");
			addressFields.add("district");
			addressFields.add("sector");
			addressFields.add("additionalAddressDetails");

			ArrayList<String> guardianFields = new ArrayList<String>();
			guardianFields.add("parentOrGuardianFirstName");
			guardianFields.add("parentOrGuardianLastName");
			guardianFields.add("parentOrGuardianRID");
			guardianFields.add("parentOrGuardianUIN");

			// Logical grouping in schema with position, precreate gridpane categoris in fxml
            Iterator<Entry<String, UiSchemaDTO>> iterator = validation.getValidationMap().entrySet().iterator();
            while (iterator.hasNext()) {
                UiSchemaDTO left = null;
                UiSchemaDTO dto = iterator.next().getValue();

				if (infoFields.contains(dto.getId())) {
					if (isDemographicField(dto)) {
						left = dto;
					}

					if (left != null) {
						UiSchemaDTO right = null;
						if (iterator.hasNext()) {
							dto = iterator.next().getValue();
							if (infoFields.contains(dto.getId()) && isDemographicField(dto)){
								right = dto;
							}
						}
						GridPane mainGridPane = addContent(left, right);
						childFlowPaneInfo.getChildren().add(mainGridPane);
						position1++;
						positionTracker.put(mainGridPane.getId(), position1);
					}
				}

                if (addressFields.contains(dto.getId())) {
                    if (isDemographicField(dto)) {
                        left = dto;
                    }

                    if (left != null) {
                        UiSchemaDTO right = null;
                        if (iterator.hasNext()) {
                            dto = iterator.next().getValue();
							if (addressFields.contains(dto.getId()) && isDemographicField(dto)) {
                                right = dto;
                            }
                        }
                        GridPane mainGridPane = addContent(left, right);
                        childFlowPaneAddress.getChildren().add(mainGridPane);
                        position2++;
                        positionTracker.put(mainGridPane.getId(), position2);
                    }
                }
                if (guardianFields.contains(dto.getId())) {
                    if (isDemographicField(dto)) {
                        left = dto;
                    }

                    if (left != null) {
                        UiSchemaDTO right = null;
                        if (iterator.hasNext()) {
                            dto = iterator.next().getValue();
							if (guardianFields.contains(dto.getId()) && isDemographicField(dto)) {
                                right = dto;
                            }
						}
                        GridPane mainGridPane = addContent(left, right);
                        childFlowPaneGuardian.getChildren().add(mainGridPane);
                        position3++;
                        positionTracker.put(mainGridPane.getId(), position3);
						mainGridPane.add(tsHBox, 2, 0);
                    }
				}
            }
			addFirstOrderAddress(listOfComboBoxWithObject.get(orderOfAddress.get(0)), 1,
					applicationContext.getApplicationLanguage());

			populateDropDowns();

			for (int j = 0; j < orderOfAddress.size() - 1; j++) {
				final int k = j;
				try {
					listOfComboBoxWithObject.get(orderOfAddress.get(k)).setOnAction((event) -> {
						configureMethodsForAddress(k, k + 1, orderOfAddress.size());
					});
				} catch (Exception runtimeException) {
					LOGGER.info(orderOfAddress.get(k) + " is not a valid field", APPLICATION_NAME,
							RegistrationConstants.APPLICATION_ID,
							runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
				}
			}

			// Toggle buttion methode application
			iniToggleButton();
			setStyle();
			continueBtn.defaultButtonProperty().bind(continueBtn.focusedProperty());

		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - CONTROLLER", APPLICATION_NAME, RegistrationConstants.APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_DEMOGRAPHIC_PAGE);

		}
	}

	private void fillOrderOfLocation() {
		List<Location> locations = masterSyncDao.getLocationDetails(applicationContext.getApplicationLanguage());
		Map<Integer, String> treeMap = new TreeMap<Integer, String>();

		Collection<UiSchemaDTO> fields = validation.getValidationMap().values();
		for (Location location : locations) {
			List<UiSchemaDTO> matchedfield = fields.stream()
					.filter(field -> isDemographicField(field) && field.getSubType() != null
							&& RegistrationConstants.DROPDOWN.equals(field.getControlType())
							&& field.getSubType().equalsIgnoreCase(location.getHierarchyName()))
					.collect(Collectors.toList());

			if (matchedfield != null && !matchedfield.isEmpty()) {
				treeMap.put(location.getHierarchyLevel(), matchedfield.get(0).getId());
				LOGGER.info("REGISTRATION - CONTROLLER", APPLICATION_NAME, RegistrationConstants.APPLICATION_ID,
						"location.getHierarchyLevel() >>> " + location.getHierarchyLevel()
								+ " matchedfield.get(0).getId() >>> " + matchedfield.get(0).getId());
			}
		}
		orderOfAddress = treeMap.values().stream().collect(Collectors.toList());
	}

	private void disablePreRegFetch() {
        childFlowPanePre.setVisible(false);
        childFlowPanePre.setManaged(false);
        childFlowPanePre.setDisable(true);
		preRegParentPane.setVisible(false);
		preRegParentPane.setManaged(false);
		preRegParentPane.setDisable(true);
	}

    public GridPane addContent(UiSchemaDTO left, UiSchemaDTO right) {
		GridPane gridPane = prepareMainGridPane();
		gridPane.setId(left.getId()+ "ParentGridPane");
		if (left != null) {
			GridPane primary = subGridPane(left, "", true);
			primary.setId(left.getId() + "ChildGridPane");
			gridPane.addColumn(0, primary);
		}
		if (right != null) {
			GridPane secondary = subGridPane(right, "", false);
			secondary.setId(right.getId() + "ChildGridPane");
			gridPane.addColumn(2, secondary);
		}
		return gridPane;
	}

	public void addKeyboard(int position) {

		if (keyboardVisible) {
			parentFlow.remove(lastPosition);
			keyboardVisible = false;
			lastPosition = position;
		} else {
			GridPane gridPane = prepareMainGridPaneForKeyboard();
			gridPane.addColumn(1, keyboardNode);
			parentFlow.add(position, gridPane);
			lastPosition = position;
			keyboardVisible = true;
		}
	}

	private GridPane prepareMainGridPane() {
		GridPane gridPane = new GridPane();
		gridPane.setPrefWidth(1000);

		ObservableList<ColumnConstraints> columnConstraints = gridPane.getColumnConstraints();
		ColumnConstraints columnConstraint1 = new ColumnConstraints();
		columnConstraint1.setPercentWidth(48);
		ColumnConstraints columnConstraint2 = new ColumnConstraints();
		columnConstraint2.setPercentWidth(4);
		ColumnConstraints columnConstraint3 = new ColumnConstraints();
		columnConstraint3.setPercentWidth(48);
		columnConstraints.addAll(columnConstraint1, columnConstraint2, columnConstraint3);
		return gridPane;
	}

	private GridPane prepareMainGridPaneForKeyboard() {
		GridPane gridPane = new GridPane();
		gridPane.setPrefWidth(1000);

		ObservableList<ColumnConstraints> columnConstraints = gridPane.getColumnConstraints();
		ColumnConstraints columnConstraint1 = new ColumnConstraints();
		columnConstraint1.setPercentWidth(10);
		ColumnConstraints columnConstraint2 = new ColumnConstraints();
		columnConstraint2.setPercentWidth(80);
		ColumnConstraints columnConstraint3 = new ColumnConstraints();
		columnConstraint3.setPercentWidth(10);
		columnConstraints.addAll(columnConstraint1, columnConstraint2, columnConstraint3);
		return gridPane;
	}

	@SuppressWarnings("unlikely-arg-type")
	public GridPane subGridPane(UiSchemaDTO schemaDTO, String languageType, Boolean isLeft) {
		GridPane gridPane = new GridPane();

		ObservableList<ColumnConstraints> columnConstraints = gridPane.getColumnConstraints();
		ColumnConstraints columnConstraint1 = new ColumnConstraints();
		if(isLeft)
        	columnConstraint1.setPercentWidth(15);
		else
			columnConstraint1.setPercentWidth(1);
		ColumnConstraints columnConstraint2 = new ColumnConstraints();
        columnConstraint2.setPercentWidth(84);
		ColumnConstraints columnConstraint3 = new ColumnConstraints();
		if(isLeft)
			columnConstraint3.setPercentWidth(1);
		else
			columnConstraint3.setPercentWidth(15);

		columnConstraints.addAll(columnConstraint1, columnConstraint2, columnConstraint3);

		ObservableList<RowConstraints> rowConstraints = gridPane.getRowConstraints();
		RowConstraints rowConstraint1 = new RowConstraints();
		columnConstraint1.setPercentWidth(20);
		RowConstraints rowConstraint2 = new RowConstraints();
		columnConstraint1.setPercentWidth(60);
		RowConstraints rowConstraint3 = new RowConstraints();
		columnConstraint1.setPercentWidth(20);
		rowConstraints.addAll(rowConstraint1, rowConstraint2, rowConstraint3);

		VBox content = null;

		switch (schemaDTO.getControlType()) {
		case RegistrationConstants.DROPDOWN:
			content = addContentWithComboBoxObject(schemaDTO.getId(), schemaDTO, languageType);
			break;
		case RegistrationConstants.AGE_DATE:
			content = addContentForDobAndAge(schemaDTO.getId(), languageType);
			break;
		case "age":
			// TODO Not yet supported
			break;
		case RegistrationConstants.TEXTBOX:
			content = addContentWithTextField(schemaDTO, schemaDTO.getId(), languageType);
			break;
		}
		gridPane.add(content, 1, 2);

		return gridPane;
	}

	public VBox addContentForDobAndAge(String fieldId, String languageType) {

		VBox vBoxDOBLabel = new VBox();
		vBoxDOBLabel.setMinWidth(127);

		VBox starVbox = new VBox();
		//startv.setMinWidth(10);

		Label dobHiddenLabel = new Label();
		dobHiddenLabel.setText(" ");
		dobHiddenLabel.setVisible(false);
		dobHiddenLabel.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_FIELD_LABEL);
		dobHiddenLabel.setId(fieldId + "__" + languageType + RegistrationConstants.LABEL);

		Label dobLabel = new Label();
		dobLabel.setMaxWidth(150);
		dobLabel.setMinHeight(43);
		dobLabel.setText(RegistrationConstants.DOBLABEL);
		dobLabel.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_FIELD_DOBLABEL);
		dobLabel.setId(fieldId + "__dobLabel" + languageType + RegistrationConstants.LABEL);
		vBoxDOBLabel.getChildren().addAll(dobHiddenLabel, dobLabel);
		VBox vBoxDD = new VBox();
		TextField dd = new TextField();
		dd.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_TEXTFIELD);
		dd.setId(fieldId + "__" + RegistrationConstants.DD + languageType);
		dd.setMinWidth(30);
		Label ddLabel = new Label();
		ddLabel.setVisible(false);
		ddLabel.setId(fieldId + "__" + RegistrationConstants.DD + languageType + RegistrationConstants.LABEL);
		ddLabel.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_FIELD_LABEL);
		vBoxDD.getChildren().addAll(ddLabel, dd);

		listOfTextField.put(fieldId + "__" + RegistrationConstants.DD + languageType, dd);

		VBox vBoxMM = new VBox();
		TextField mm = new TextField();
		mm.setMinWidth(40);
		mm.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_TEXTFIELD);
		mm.setId(fieldId + "__" + RegistrationConstants.MM + languageType);
		Label mmLabel = new Label();
		mmLabel.setVisible(false);
		mmLabel.setId(fieldId + "__" + RegistrationConstants.MM + languageType + RegistrationConstants.LABEL);
		mmLabel.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_FIELD_LABEL);
		vBoxMM.getChildren().addAll(mmLabel, mm);

		listOfTextField.put(fieldId + "__" + RegistrationConstants.MM + languageType, mm);

		VBox vBoxYYYY = new VBox();
		TextField yyyy = new TextField();
		yyyy.setMinWidth(52);
		yyyy.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_TEXTFIELD);
		yyyy.setId(fieldId + "__" + RegistrationConstants.YYYY + languageType);
		Label yyyyLabel = new Label();
		yyyyLabel.setVisible(false);
		yyyyLabel.setId(fieldId + "__" + RegistrationConstants.YYYY + languageType + RegistrationConstants.LABEL);
		yyyyLabel.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_FIELD_LABEL);
		vBoxYYYY.getChildren().addAll(yyyyLabel, yyyy);

		listOfTextField.put(fieldId + "__" + RegistrationConstants.YYYY + languageType, yyyy);

		Label dobMessage = new Label();
		dobMessage.setId(fieldId + "__" + RegistrationConstants.DOB_MESSAGE + languageType);
		dobMessage.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_FIELD_LABEL);

		boolean localLanguage = languageType.equals(RegistrationConstants.LOCAL_LANGUAGE);

		dd.setPromptText(localLanguage ? localLabelBundle.getString(RegistrationConstants.DD)
				: applicationLabelBundle.getString(RegistrationConstants.DD));
		ddLabel.setText(localLanguage ? localLabelBundle.getString(RegistrationConstants.DD)
				: applicationLabelBundle.getString(RegistrationConstants.DD));
		mm.setPromptText(localLanguage ? localLabelBundle.getString(RegistrationConstants.MM)

				: applicationLabelBundle.getString(RegistrationConstants.MM));
		mmLabel.setText(localLanguage ? localLabelBundle.getString(RegistrationConstants.MM)
				: applicationLabelBundle.getString(RegistrationConstants.MM));
		dobMessage.setText("");

		yyyy.setPromptText(localLanguage ? localLabelBundle.getString(RegistrationConstants.YYYY)
				: applicationLabelBundle.getString(RegistrationConstants.YYYY));
		yyyyLabel.setText(localLanguage ? localLabelBundle.getString(RegistrationConstants.YYYY)
				: applicationLabelBundle.getString(RegistrationConstants.YYYY));

		HBox hB = new HBox();
		hB.setSpacing(2);
		hB.getChildren().addAll(vBoxDOBLabel,starVbox, vBoxDD, vBoxMM, vBoxYYYY);

		VBox vboxAgeField = new VBox();
		TextField ageField = new TextField();
		ageField.setMinWidth(45);
		ageField.setId(fieldId + "__" + RegistrationConstants.AGE_FIELD + languageType);
		ageField.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_TEXTFIELD);
		Label ageFieldLabel = new Label();
		ageFieldLabel.setId(fieldId + "__" + RegistrationConstants.AGE_FIELD + languageType + RegistrationConstants.LABEL);
		ageFieldLabel.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_FIELD_LABEL);
		ageFieldLabel.setVisible(false);
		vboxAgeField.getChildren().addAll(ageFieldLabel, ageField);

		listOfTextField.put(RegistrationConstants.AGE_FIELD + languageType, ageField);

		ageField.setPromptText(localLanguage ? localLabelBundle.getString(RegistrationConstants.AGE_FIELD)
				: applicationLabelBundle.getString(RegistrationConstants.AGE_FIELD));
		ageFieldLabel.setText(localLanguage ? localLabelBundle.getString(RegistrationConstants.AGE_FIELD)
				: applicationLabelBundle.getString(RegistrationConstants.AGE_FIELD));

		hB.setMaxWidth(200);

		Label orLabel = new Label(localLanguage ? localLabelBundle.getString("ageOrDOBField")
				: applicationLabelBundle.getString("ageOrDOBField"));
        orLabel.setStyle("-fx-text-fill:#020F59");

		VBox orVbox = new VBox();
		orVbox.setMinWidth(30);
		orVbox.setSpacing(15);
		orVbox.getChildren().addAll(new Label(), orLabel);

		HBox hB2 = new HBox();
		hB2.setSpacing(5);
		hB2.getChildren().addAll(hB, orVbox, vboxAgeField);

		VBox vB = new VBox();
		vB.getChildren().addAll(hB2, dobMessage);

		ageBasedOperation(vB, ageField, dobMessage, dd, mm, yyyy);

		fxUtils.focusedAction(hB, dd);
		fxUtils.focusedAction(hB, mm);
		fxUtils.focusedAction(hB, yyyy);

		dateValidation.validateDate(parentFlowPane, dd, mm, yyyy, validation, fxUtils, ageField, null, dobMessage);
		dateValidation.validateMonth(parentFlowPane, dd, mm, yyyy, validation, fxUtils, ageField, null, dobMessage);
		dateValidation.validateYear(parentFlowPane, dd, mm, yyyy, validation, fxUtils, ageField, null, dobMessage);

		vB.setDisable(languageType.equals(RegistrationConstants.LOCAL_LANGUAGE));

		return vB;
	}

	@Autowired
	ResourceLoader resourceLoader;

	public VBox addContentWithTextField(UiSchemaDTO schema, String fieldName, String languageType) {
		/*limit the pre registration field caracter to 14 numbers */
		addTextLimiter(preRegistrationId,14);
		// force the pre registration field to be numeric only
		preRegistrationId.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue,
								String newValue) {
				if (!newValue.matches("\\d*")) {
					preRegistrationId.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});

		TextField field = new TextField();
        //Formating the TextField caps automatically
        field.setTextFormatter(new TextFormatter<>((change) -> {
            change.setText(change.getText().toUpperCase());
            return change;
        }));
		Label label = new Label();
		Label validationMessage = new Label();
		Label star = new Label();
		HBox hB = new HBox();
		//start.setMinHeight(10);
		star.setText(RegistrationConstants.STAR);
		// star.setStyle("-fx-text-fill: red;-fx-prompt-text-fill: red;");
		star.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_FIELD_STAR);
		VBox vbox = new VBox();
		vbox.setId(fieldName + RegistrationConstants.Parent);
		field.setId(fieldName + languageType);
		field.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_TEXTFIELD);
		label.setId(fieldName + languageType + RegistrationConstants.LABEL);
		label.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_FIELD_LABEL);
		label.setVisible(false);
		validationMessage.setId(fieldName + languageType + RegistrationConstants.MESSAGE);
		validationMessage.getStyleClass().add(RegistrationConstants.DemoGraphicFieldMessageLabel);
		label.setPrefWidth(vbox.getPrefWidth());
		field.setPrefWidth(vbox.getPrefWidth());
		validationMessage.setPrefWidth(vbox.getPrefWidth());
		vbox.setSpacing(5);
		vbox.getChildren().add(label);
		//vbox.getChildren().addAll(field,star);
        vbox.getChildren().addAll(field);
		//HBox hB = new HBox();
		//hB.setSpacing(20);

		vbox.getChildren().add(validationMessage);
		if (listOfTextField.get(fieldName) != null)
			fxUtils.populateLocalFieldWithFocus(parentFlowPane, listOfTextField.get(fieldName), field,
					hasToBeTransliterated, validation);
		listOfTextField.put(field.getId(), field);
		if (!(fieldName.equalsIgnoreCase(
				"phone") || fieldName.equalsIgnoreCase("email") || fieldName.equalsIgnoreCase("additionalAddressDetails")))
		{
				field.setPromptText(schema.getLabel().get(RegistrationConstants.PRIMARY) + RegistrationConstants.STARWITHSPACE);
				putIntoLabelMap(fieldName + languageType, schema.getLabel().get(RegistrationConstants.PRIMARY));
				label.setText(schema.getLabel().get(RegistrationConstants.PRIMARY) + RegistrationConstants.STARWITHSPACE );
		}
		 //complement address limit max caracteres 40
		else if(fieldName.equalsIgnoreCase("additionalAddressDetails")) {
			addTextLimiter(field,40);
			field.setPromptText(schema.getLabel().get(RegistrationConstants.PRIMARY));
			putIntoLabelMap(fieldName + languageType, schema.getLabel().get(RegistrationConstants.PRIMARY));
			label.setText(schema.getLabel().get(RegistrationConstants.PRIMARY));
			field.focusedProperty().addListener(new ChangeListener<Boolean>()
			{
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
				{
					if (newPropertyValue)
					{
						field.setPromptText(schema.getLabel().get(RegistrationConstants.PRIMARY) + RegistrationConstants.LIMIT_CARACTERES);
					}
					else
					{
						field.setPromptText(schema.getLabel().get(RegistrationConstants.PRIMARY));
						putIntoLabelMap(fieldName + languageType, schema.getLabel().get(RegistrationConstants.PRIMARY));
						label.setText(schema.getLabel().get(RegistrationConstants.PRIMARY));
					}
				}
			});
		}
		else{
			field.setPromptText(schema.getLabel().get(RegistrationConstants.PRIMARY));
			putIntoLabelMap(fieldName + languageType, schema.getLabel().get(RegistrationConstants.PRIMARY));
			label.setText(schema.getLabel().get(RegistrationConstants.PRIMARY));
		}
		fxUtils.onTypeFocusUnfocusListener(parentFlowPane, field);
		return vbox;
	}

	private void populateDropDowns() {
		try {
			for (String k : listOfComboBoxWithObject.keySet()) {
				switch (k.toLowerCase()) {
				case "gender":
					listOfComboBoxWithObject.get("gender").getItems()
							.addAll(masterSyncService.getGenderDtls(ApplicationContext.applicationLanguage()).stream()
									.filter(v -> !v.getCode().equals("OTH")).collect(Collectors.toList()));
					break;

				case "residencestatus":
					listOfComboBoxWithObject.get("residenceStatus").getItems()
							.addAll(masterSyncService.getIndividualType(ApplicationContext.applicationLanguage()));
					break;

				default:
					// TODO
					break;
				}
			}
		} catch (RegBaseCheckedException e) {
			LOGGER.error("populateDropDowns", APPLICATION_NAME, RegistrationConstants.APPLICATION_ID,
					ExceptionUtils.getStackTrace(e));
		}
	}

	public <T> VBox addContentWithComboBoxObject(String fieldName, UiSchemaDTO schema, String languageType) {

		ComboBox<GenericDto> field = new ComboBox<GenericDto>();
		field.setMaxSize(380,30);
		Label label = new Label();
		Label validationMessage = new Label();
		StringConverter<T> uiRenderForComboBox = fxUtils.getStringConverterForComboBox();
		VBox vbox = new VBox();
		field.setId(fieldName + languageType);
		field.setPrefWidth(vbox.getPrefWidth());
		if (listOfComboBoxWithObject.get(fieldName) != null) {
			fxUtils.populateLocalComboBox(parentFlowPane, listOfComboBoxWithObject.get(fieldName), field);
		}
		helperMethodForComboBox(field, fieldName, schema, label, validationMessage, vbox, languageType);
		field.setConverter((StringConverter<GenericDto>) uiRenderForComboBox);
		listOfComboBoxWithObject.put(fieldName + languageType, field);

		fxUtils.onTypeFocusUnfocusListenerCombo(parentFlowPane, field);
		field.setPromptText(schema.getLabel().get(RegistrationConstants.PRIMARY) + RegistrationConstants.STARWITHSPACE);
		label.setText(schema.getLabel().get(RegistrationConstants.PRIMARY) + RegistrationConstants.STARWITHSPACE);
		return vbox;
	}

	/**
	 * setting the registration navigation label to lost uin
	 */
	protected void lostUIN() {
		lostUIN = true;
		registrationNavlabel
				.setText(ApplicationContext.applicationLanguageBundle().getString(RegistrationConstants.LOSTUINLBL));
	}

	/**
	 * To restrict the user not to eavcdnter any values other than integer values.
	 */
	private void ageBasedOperation(Pane parentPane, TextField ageField, Label dobMessage, TextField dd, TextField mm,
			TextField yyyy) {
		try {
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Validating the age given by age field");
			fxUtils.validateLabelFocusOut(parentPane, ageField);
			ageField.focusedProperty().addListener((obsValue, oldValue, newValue) -> {
				if (!getAge(yyyy.getText(), mm.getText(), dd.getText()).equals(ageField.getText())) {
					if (oldValue) {
						ageValidation(parentPane, ageField, dobMessage, oldValue, dd, mm, yyyy);
					}
				}
			});


			ageField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {

                    int age = 0;
                    try {
                        age = Integer.parseInt(newValue);
                    } catch (NumberFormatException nfe) {
                        age = 0;
                    }
                    ((TextField) parentFlowPane.lookup(RegistrationConstants.HASH.concat("parentOrGuardianFirstName"))).setText("");
                    ((TextField) parentFlowPane.lookup(RegistrationConstants.HASH.concat("parentOrGuardianLastName"))).setText("");
                    ((TextField) parentFlowPane.lookup(RegistrationConstants.HASH.concat("parentOrGuardianUIN"))).setText("");
                    ((TextField) parentFlowPane.lookup(RegistrationConstants.HASH.concat("parentOrGuardianRID"))).setText("");

                    if (age>=0 && age < RegistrationConstants.MajorityAge) {
                        childFlowPaneGuardian.setVisible(true);
                    } else {
                        childFlowPaneGuardian.setVisible(false);
                    }
                }
            });
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Validating the age given by age field");
		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - AGE FIELD VALIDATION FAILED ", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
		}
	}

	/**
	 * Gets the age.
	 *
	 * @param year
	 *            the year
	 * @param month
	 *            the month
	 * @param date
	 *            the date
	 * @return the age
	 */
	String getAge(String year, String month, String date) {
		if (year != null && !year.isEmpty() && month != null && !month.isEmpty() && date != null && !date.isEmpty()) {
			LocalDate birthdate = new LocalDate(Integer.parseInt(year), Integer.parseInt(month),
					Integer.parseInt(date)); // Birth
												// date
			LocalDate now = new LocalDate(); // Today's date

			Period period = new Period(birthdate, now, PeriodType.yearMonthDay());
			return String.valueOf(period.getYears());
		} else {
			return "";
		}
	}
	public void ageValidation(Pane dobParentPane, TextField ageField, Label dobMessage, Boolean oldValue, TextField dd,
			TextField mm, TextField yyyy) {
		//control negatif value input
		if(Integer.parseInt(ageField.getText() )< 0 )
		{
			dobMessage.setText(RegistrationUIConstants.INVALID_DATE);
			ageField.clear();
			dobMessage.setVisible(true);
			dobMessage.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_FIELD_MESSAGELABEL);
		}else
		if (ageField.getText().matches(RegistrationConstants.NUMBER_OR_NOTHING_REGEX)) {
			if (ageField.getText().matches(RegistrationConstants.NUMBER_REGEX)) {
				if (maxAge >= Integer.parseInt(ageField.getText())) {
					age = Integer.parseInt(ageField.getText());

					// When user enter age, the estimated mm = 01, and dd = 01
					Calendar defaultDate = Calendar.getInstance();
					defaultDate.add(Calendar.YEAR, -age);

					dd.setText("01");
					mm.setText("01");
					String yyyyText = String.valueOf(defaultDate.get(Calendar.YEAR));
					yyyy.setText(yyyyText);
					dd.requestFocus();

					// TODO NOT REQUIRED NOW
					/*
					 * if (age <= minAge) {
					 *
					 * if (!isChild == true) { clearAllValues(); clearAllBiometrics(); } if
					 * (RegistrationConstants.DISABLE.equalsIgnoreCase(
					 * getValueFromApplicationContext(RegistrationConstants.FINGERPRINT_DISABLE_FLAG
					 * )) && RegistrationConstants.DISABLE.equalsIgnoreCase(
					 * getValueFromApplicationContext(RegistrationConstants.IRIS_DISABLE_FLAG))) {
					 * isChild = true; validation.setChild(isChild);
					 * generateAlert(RegistrationConstants.ERROR,
					 * RegistrationUIConstants.PARENT_BIO_MSG);
					 *
					 * } else { updatePageFlow(RegistrationConstants.GUARDIAN_BIOMETRIC, true);
					 * updatePageFlow(RegistrationConstants.FINGERPRINT_CAPTURE, false);
					 * updatePageFlow(RegistrationConstants.IRIS_CAPTURE, false);
					 *
					 * } } else {
					 *
					 * if (!isChild == false) { clearAllValues(); clearAllBiometrics(); } if
					 * (getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO(
					 * ) != null) {
					 *
					 * getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO()
					 * .setFingerprintDetailsDTO(new ArrayList<>());
					 *
					 * getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO()
					 * .setIrisDetailsDTO(new ArrayList<>());
					 *
					 * getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO()
					 * .setBiometricExceptionDTO(new ArrayList<>());
					 *
					 * getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO()
					 * .setExceptionFace(new FaceDetailsDTO());
					 *
					 * getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO()
					 * .setFace(new FaceDetailsDTO());
					 *
					 * getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO()
					 * .setHasExceptionPhoto(false);
					 *
					 * }
					 *
					 * updatePageFlow(RegistrationConstants.GUARDIAN_BIOMETRIC, false); //
					 * updateBioPageFlow(RegistrationConstants.FINGERPRINT_DISABLE_FLAG, //
					 * RegistrationConstants.FINGERPRINT_CAPTURE); //
					 * updateBioPageFlow(RegistrationConstants.IRIS_DISABLE_FLAG, //
					 * RegistrationConstants.IRIS_CAPTURE);
					 *
					 * }
					 */
					fxUtils.validateOnFocusOut(dobParentPane, ageField, validation, false);
				} else {
					ageField.getStyleClass().remove("demoGraphicTextFieldOnType");
					ageField.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_TEXTFIELD_FOCUSED);
					Label ageFieldLabel = (Label) dobParentPane
							.lookup("#" + ageField.getId() + RegistrationConstants.LABEL);
					ageFieldLabel.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_FIELD_LABEL);
					ageField.getStyleClass().remove("demoGraphicFieldLabelOnType");
					dobMessage.setText(RegistrationUIConstants.INVALID_AGE + maxAge);
					dobMessage.setVisible(true);

					generateAlert(dobParentPane, RegistrationConstants.DOB, dobMessage.getText());
					// parentFieldValidation();
				}
			} else {
				// updatePageFlow(RegistrationConstants.GUARDIAN_BIOMETRIC, false);
				// updatePageFlow(RegistrationConstants.FINGERPRINT_CAPTURE, true);
				// updatePageFlow(RegistrationConstants.IRIS_CAPTURE, true);
				//dobMessage.setText(RegistrationUIConstants.INVALID_DATE);
				dd.clear();
				mm.clear();
				yyyy.clear();
			}
		} else {
			ageField.setText(RegistrationConstants.EMPTY);
		}
	}

	private void addFirstOrderAddress(ComboBox<GenericDto> location, int id, String languageType) {
		//location.setMinSize(280,30);
		if (location != null) {
			location.getItems().clear();
			try {
				List<GenericDto> locations = null;
				locations = masterSync.findLocationByHierarchyCode(id, languageType);

				if (locations.isEmpty()) {
					GenericDto lC = new GenericDto();
					lC.setCode(RegistrationConstants.AUDIT_DEFAULT_USER);
					lC.setName(RegistrationConstants.AUDIT_DEFAULT_USER);
					lC.setLangCode(ApplicationContext.applicationLanguage());
					location.getItems().add(lC) ;
				} else {
					location.getItems().addAll(locations);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private List<GenericDto> LocationDtoToComboBoxDto(List<LocationDto> locations) {
		List<GenericDto> listOfValues = new ArrayList<>();
		for (LocationDto locationDto : locations) {
			GenericDto comboBox = new GenericDto();
			comboBox.setCode(locationDto.getCode());
			comboBox.setName(locationDto.getName());
			comboBox.setLangCode(locationDto.getLangCode());
			listOfValues.add(comboBox);
		}
		return listOfValues;
	}

	private void addDemoGraphicDetailsToSession() {
		try {
			RegistrationDTO registrationDTO = getRegistrationDTOFromSession();
			for (UiSchemaDTO schemaField : validation.getValidationMap().values()) {
				if (schemaField.getControlType() == null)
					continue;

				if (registrationDTO.getRegistrationCategory().equals(RegistrationConstants.PACKET_TYPE_UPDATE)
						&& !registrationDTO.getUpdatableFields().contains(schemaField.getId()))
					continue;

				switch (schemaField.getType()) {
				case RegistrationConstants.SIMPLE_TYPE:
					if (schemaField.getControlType().equals(RegistrationConstants.DROPDOWN)) {
						ComboBox<GenericDto> platformField = listOfComboBoxWithObject.get(schemaField.getId());
						ComboBox<GenericDto> localField = listOfComboBoxWithObject
								.get(schemaField.getId() + RegistrationConstants.LOCAL_LANGUAGE);
						registrationDTO.addDemographicField(schemaField.getId(),
								applicationContext.getApplicationLanguage(),
								platformField == null ? null
										: platformField.getValue() != null ? platformField.getValue().getName() : null,
								applicationContext.getLocalLanguage(), localField == null ? null
										: localField.getValue() != null ? localField.getValue().getName() : null);
					} else {
						TextField platformField = listOfTextField.get(schemaField.getId());
						TextField localField = listOfTextField
								.get(schemaField.getId() + RegistrationConstants.LOCAL_LANGUAGE);
						registrationDTO.addDemographicField(schemaField.getId(),
								applicationContext.getApplicationLanguage(), platformField.getText(),
								applicationContext.getLocalLanguage(),
								localField == null ? null : localField.getText());
					}
					break;
				case RegistrationConstants.NUMBER:
				case RegistrationConstants.STRING:
					if (schemaField.getControlType().equalsIgnoreCase(RegistrationConstants.AGE_DATE))
						registrationDTO.setDateField(schemaField.getId(),
								listOfTextField.get(schemaField.getId() + "__" + RegistrationConstants.DD).getText(),
								listOfTextField.get(schemaField.getId() + "__" + RegistrationConstants.MM).getText(),
								listOfTextField.get(schemaField.getId() + "__" + RegistrationConstants.YYYY).getText());
					else {
						if (schemaField.getControlType().equals(RegistrationConstants.DROPDOWN)) {
							ComboBox<GenericDto> platformField = listOfComboBoxWithObject.get(schemaField.getId());
							registrationDTO.addDemographicField(schemaField.getId(), platformField == null ? null
									: platformField.getValue() != null ? platformField.getValue().getName() : null);
						} else {
							TextField platformField = listOfTextField.get(schemaField.getId());
							registrationDTO.addDemographicField(schemaField.getId(),
									platformField != null ? platformField.getText() : null);
						}
					}
					break;

				default:
					break;
				}
			}
		} catch (Exception exception) {
			LOGGER.error("addDemoGraphicDetailsToSession", APPLICATION_NAME, RegistrationConstants.APPLICATION_ID,
					exception.getMessage() + ExceptionUtils.getStackTrace(exception));
		}
	}

	/**
	 * To load the provinces in the selection list based on the language code
	 */
	private void configureMethodsForAddress(int s, int p, int size) {
		try {
			retrieveAndPopulateLocationByHierarchy(listOfComboBoxWithObject.get(orderOfAddress.get(s)),
					listOfComboBoxWithObject.get(orderOfAddress.get(p)),
					listOfComboBoxWithObject.get(orderOfAddress.get(p) + RegistrationConstants.LOCAL_LANGUAGE));

			for (int i = p + 1; i < size; i++) {
				listOfComboBoxWithObject.get(orderOfAddress.get(i)).getItems().clear();
			}

		} catch (RuntimeException runtimeException) {
			LOGGER.error(" failed due to invalid field", APPLICATION_NAME, RegistrationConstants.APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
		}

	}

	/**
	 *
	 * Saving the detail into concerned DTO'S
	 *
	 */
	public void saveDetail() {
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Saving the fields to DTO");
		try {
			auditFactory.audit(AuditEvent.SAVE_DETAIL_TO_DTO, Components.REGISTRATION_CONTROLLER,
					SessionContext.userContext().getUserId(), RegistrationConstants.ONBOARD_DEVICES_REF_ID_TYPE);

			RegistrationDTO registrationDTO = getRegistrationDTOFromSession();
			/*
			 * if (preRegistrationId.getText() == null &&
			 * preRegistrationId.getText().isEmpty()) {
			 * registrationDTO.setPreRegistrationId(""); }
			 */

			addDemoGraphicDetailsToSession();

			SessionContext.map().put(RegistrationConstants.IS_Child, registrationDTO.isChild());

			registrationDTO.getOsiDataDTO().setOperatorID(SessionContext.userContext().getUserId());

			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Saved the demographic fields to DTO");

		} catch (Exception exception) {
			LOGGER.error("REGISTRATION - SAVING THE DETAILS FAILED ", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID,
					exception.getMessage() + ExceptionUtils.getStackTrace(exception));
		}
	}

	public void uinUpdate() {
		List<String> selectionList = getRegistrationDTOFromSession().getUpdatableFields();
		String childNodeId = "";
		if (selectionList != null) {
			disablePreRegFetch();
			registrationNavlabel.setText(applicationLabelBundle.getString("uinUpdateNavLbl"));
			for (Node pane : childFlowPaneInfo.getChildren()) {
				GridPane gp = (GridPane) pane;
				for (Node childNode: gp.getChildren()){
					childNodeId = childNode.getId();
					if (childNode.getId() != null && childNode.getId().matches("(.*)ChildGridPane")) {
						String fieldId = childNode.getId().replace("ChildGridPane", "");
						if (selectionList.contains(fieldId)) {
							childNode.setDisable(false);
						} else {
							UiSchemaDTO schemaField = getValidationMap().get(fieldId);
							childNode.setDisable(schemaField != null && ("name".equalsIgnoreCase(schemaField.getSubType()) || "dob".equalsIgnoreCase(schemaField.getSubType())) ? false
									: true);
						}
					}
				}
			//	parentFlowPane.getChildren();
			}
			for (Node pane : childFlowPaneAddress.getChildren()) {
				if (!pane.getId().matches("languageLabelParentPane|languageLabelParentPane")) {
					GridPane gp = (GridPane) pane;
					for (Node childNode: gp.getChildren()){
						childNodeId = childNode.getId();
						if (childNode.getId() != null && childNode.getId().matches("(.*)ChildGridPane")) {
							String fieldId = childNode.getId().replace("ChildGridPane", "");
							if (selectionList.contains(fieldId)) {
								childNode.setDisable(false);
							} else {
								childNode.setDisable(true);
							}
						}
					}
				}
				//parentFlowPane.getChildren();
			}
			childFlowPaneGuardian.setVisible(false);
//			for (Node pane : childFlowPaneGuardian.getChildren()) {
//				if (!pane.getId().matches("languageLabelParentPane1|languageLabelParentPane")) {
//					GridPane gp = (GridPane) pane;
//					for (Node childNode: gp.getChildren()){
//						childNodeId = childNode.getId();
//						if (childNode.getId() != null && childNode.getId().matches("(.*)ChildGridPane")) {
//							String fieldId = childNode.getId().replace("ChildGridPane", "");
//							if (selectionList.contains(fieldId)) {
//								childNode.setDisable(false);
//							} else {
//								childNode.setDisable(true);
//							}
//						}
//					}
//				}
//				//parentFlowPane.getChildren();
//			}

		}
	}

	/**
	 * This method is to prepopulate all the values for edit operation
	 */
	public void prepareEditPageContent() {
		try {
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Preparing the Edit page content");

			RegistrationDTO registrationDTO = getRegistrationDTOFromSession();
			Map<String, Object> demographics = registrationDTO.getDemographics();

			for (UiSchemaDTO schemaField : validation.getValidationMap().values()) {
				Object value = demographics.get(schemaField.getId());
				if (value == null)
					continue;

				switch (schemaField.getType()) {
				case RegistrationConstants.SIMPLE_TYPE:
					if (schemaField.getControlType().equals(RegistrationConstants.DROPDOWN)
							|| Arrays.asList(orderOfAddress).contains(schemaField.getId())) {
						populateFieldValue(listOfComboBoxWithObject.get(schemaField.getId()),
								listOfComboBoxWithObject
										.get(schemaField.getId() + RegistrationConstants.LOCAL_LANGUAGE),
								(List<SimpleDto>) value);
					} else
						populateFieldValue(listOfTextField.get(schemaField.getId()),
								listOfTextField.get(schemaField.getId() + RegistrationConstants.LOCAL_LANGUAGE ),
								(List<SimpleDto>) value);
					break;

				case RegistrationConstants.NUMBER:
				case RegistrationConstants.STRING:
					if (RegistrationConstants.AGE_DATE.equalsIgnoreCase(schemaField.getControlType())) {
						String[] dateParts = ((String) value).split("/");
						if (dateParts.length == 3) {
							listOfTextField.get(schemaField.getId() + "__" + "dd").setText(dateParts[2]);
							listOfTextField.get(schemaField.getId() + "__" + "mm").setText(dateParts[1]);
							listOfTextField.get(schemaField.getId() + "__" + "yyyy").setText(dateParts[0]);
						}
					} else if (RegistrationConstants.DROPDOWN.equalsIgnoreCase(schemaField.getControlType())
							|| Arrays.asList(orderOfAddress).contains(schemaField.getId())) {
						ComboBox<GenericDto> platformField = listOfComboBoxWithObject.get(schemaField.getId());
						if (platformField != null) {
							platformField.setValue(new GenericDto((String) value, (String) value, "eng"));
						}
					} else {
						TextField textField = listOfTextField.get(schemaField.getId());
						if (textField != null)
							textField.setText(value.toString());
					}
				}
			}
	preRegistrationId.setText(registrationDTO.getPreRegistrationId());

		} catch (RuntimeException runtimeException) {
			LOGGER.error(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
		}

	}

	/**
	 * Method to populate the local field value
	 *
	 */
	private void populateFieldValue(Node nodeForPlatformLang, Node nodeForLocalLang, List<SimpleDto> fieldValues) {
		if (fieldValues != null) {
			String platformLanguageCode = applicationContext.getApplicationLanguage();
			String localLanguageCode = applicationContext.getLocalLanguage();
			String valueInPlatformLang = RegistrationConstants.EMPTY;
			String valueinLocalLang = RegistrationConstants.EMPTY;

			for (SimpleDto fieldValue : fieldValues) {
				if (fieldValue.getLanguage().equalsIgnoreCase(platformLanguageCode)) {
					valueInPlatformLang = fieldValue.getValue();
				} else if (nodeForLocalLang != null && fieldValue.getLanguage().equalsIgnoreCase(localLanguageCode)) {
					valueinLocalLang = fieldValue.getValue();
				}
			}

			if (nodeForPlatformLang instanceof TextField) {
				((TextField) nodeForPlatformLang).setText(valueInPlatformLang);

				if (nodeForLocalLang != null) {
					((TextField) nodeForLocalLang).setText(valueinLocalLang);
				}
			} else if (nodeForPlatformLang instanceof ComboBox) {
				fxUtils.selectComboBoxValue((ComboBox<?>) nodeForPlatformLang, valueInPlatformLang);
			}
		}
	}

	/**
	 * Method to fetch the pre-Registration details
	 */
	@FXML
	private void fetchPreRegistration() {

		String preRegId = preRegistrationId.getText();

		if (StringUtils.isEmpty(preRegId)) {
			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.PRE_REG_ID_EMPTY);
			return;
		} else {
			try {
				pridValidatorImpl.validateId(preRegId);
			} catch (InvalidIDException invalidIDException) {
				generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.PRE_REG_ID_NOT_VALID);
				LOGGER.error("PRID VALIDATION FAILED", APPLICATION_NAME, RegistrationConstants.APPLICATION_ID,
						invalidIDException.getMessage() + ExceptionUtils.getStackTrace(invalidIDException));
				return;
			}
		}

		auditFactory.audit(AuditEvent.REG_DEMO_PRE_REG_DATA_FETCH, Components.REG_DEMO_DETAILS, SessionContext.userId(),
				AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

		registrationController.createRegistrationDTOObject(RegistrationConstants.PACKET_TYPE_NEW);
		documentScanController.clearDocSection();

		ResponseDTO responseDTO = preRegistrationDataSyncService.getPreRegistration(preRegId);

		SuccessResponseDTO successResponseDTO = responseDTO.getSuccessResponseDTO();
		List<ErrorResponseDTO> errorResponseDTOList = responseDTO.getErrorResponseDTOs();

		if (successResponseDTO != null && successResponseDTO.getOtherAttributes() != null
				&& successResponseDTO.getOtherAttributes().containsKey(RegistrationConstants.REGISTRATION_DTO)) {
			SessionContext.map().put(RegistrationConstants.REGISTRATION_DATA,
					successResponseDTO.getOtherAttributes().get(RegistrationConstants.REGISTRATION_DTO));
			getRegistrationDTOFromSession().setPreRegistrationId(preRegId);
			prepareEditPageContent();

		} else if (errorResponseDTOList != null && !errorResponseDTOList.isEmpty()) {
			generateAlertLanguageSpecific(RegistrationConstants.ERROR, errorResponseDTOList.get(0).getMessage());
		}
	}

	/**
	 *
	 * Setting the focus to specific fields when keyboard loads
	 *
	 */
	private void setFocusonLocalField(MouseEvent event) {
		try {
			Node node = (Node) event.getSource();
			listOfTextField.get(node.getId() + "LocalLanguage").requestFocus();
			keyboardNode.setVisible(true);
			keyboardNode.setManaged(true);
			addKeyboard(positionTracker.get((node.getId() + "ParentGridPane")) + 1);

		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - SETTING FOCUS ON LOCAL FIELD FAILED", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
		}
	}

	/**
	 * Method to go back to previous page
	 */

	@FXML
	private void back() {
		try {
			if (getRegistrationDTOFromSession().getSelectionListDTO() != null) {
				Parent uinUpdate = BaseController.load(getClass().getResource(RegistrationConstants.UIN_UPDATE));
				getScene(uinUpdate);
			} else {
				goToHomePageFromRegistration();
			}
		} catch (IOException exception) {
			LOGGER.error("COULD NOT LOAD HOME PAGE", APPLICATION_NAME, RegistrationConstants.APPLICATION_ID,
					exception.getMessage() + ExceptionUtils.getStackTrace(exception));
		}
	}
	/**
	 * Method to go back to next page
	 */

	@FXML
	private void next() throws InvalidApplicantArgumentException, ParseException {

		if (preRegistrationId.getText().isEmpty()) {
			preRegistrationId.clear();
		}

		// Its required to save before validation as on spot check for values during
		// MVEL validation
		saveDetail();

		if (registrationController.validateDemographicPane(parentFlowPane)) {
			// saveDetail();

			guardianBiometricsController.populateBiometricPage(false, false);
			/*
			 * SessionContext.map().put("demographicDetail", false);
			 * SessionContext.map().put("documentScan", true);
			 */

			documentScanController.populateDocumentCategories();

			auditFactory.audit(AuditEvent.REG_DEMO_NEXT, Components.REG_DEMO_DETAILS, SessionContext.userId(),
					AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

			registrationController.showCurrentPage(RegistrationConstants.DEMOGRAPHIC_DETAIL,
					getPageByAction(RegistrationConstants.DEMOGRAPHIC_DETAIL, RegistrationConstants.NEXT));

		}
	}

	/**
	 * Retrieving and populating the location by hierarchy
	 */
	private void retrieveAndPopulateLocationByHierarchy(ComboBox<GenericDto> srcLocationHierarchy,
			ComboBox<GenericDto> destLocationHierarchy, ComboBox<GenericDto> destLocationHierarchyInLocal) {
		LOGGER.info("REGISTRATION - INDIVIDUAL_REGISTRATION - RETRIEVE_AND_POPULATE_LOCATION_BY_HIERARCHY",
				RegistrationConstants.APPLICATION_ID, RegistrationConstants.APPLICATION_NAME,
				"Retrieving and populating of location by selected hirerachy started");

		try {

			GenericDto selectedLocationHierarchy = srcLocationHierarchy.getSelectionModel().getSelectedItem();
			if (selectedLocationHierarchy != null) {
				destLocationHierarchy.getItems().clear();
//				destLocationHierarchyInLocal.getItems().clear();

				if (selectedLocationHierarchy.getCode().equalsIgnoreCase(RegistrationConstants.AUDIT_DEFAULT_USER)) {
					destLocationHierarchy.getItems().add(selectedLocationHierarchy);
				} else {

					List<GenericDto> locations = masterSync.findProvianceByHierarchyCode(
							selectedLocationHierarchy.getCode(), selectedLocationHierarchy.getLangCode());

					if (locations.isEmpty()) {
						GenericDto lC = new GenericDto();
						lC.setCode(RegistrationConstants.AUDIT_DEFAULT_USER);
						lC.setName(RegistrationConstants.AUDIT_DEFAULT_USER);
						lC.setLangCode(ApplicationContext.applicationLanguage());
						destLocationHierarchy.getItems().add(lC);
						destLocationHierarchyInLocal.getItems().add(lC);
					} else {
						destLocationHierarchy.getItems().addAll(locations);
					}
				}
			}
		} catch (RuntimeException | RegBaseCheckedException runtimeException) {
			LOGGER.error("REGISTRATION - INDIVIDUAL_REGISTRATION - RETRIEVE_AND_POPULATE_LOCATION_BY_HIERARCHY",
					APPLICATION_NAME, RegistrationConstants.APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
		}
		LOGGER.info("REGISTRATION - INDIVIDUAL_REGISTRATION - RETRIEVE_AND_POPULATE_LOCATION_BY_HIERARCHY",
				RegistrationConstants.APPLICATION_ID, RegistrationConstants.APPLICATION_NAME,
				"Retrieving and populating of location by selected hirerachy ended");
	}

}
