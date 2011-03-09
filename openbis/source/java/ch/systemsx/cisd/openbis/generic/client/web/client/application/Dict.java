/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

/**
 * Contains keys of the localized messages.
 * <p>
 * Use these constants instead of accessing messages with hard coded keys! Consider extending this
 * class for plugin specific keys. Currently this class contains message keys of the <i>common</i>
 * and <i>generic</i> technology.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public abstract class Dict
{

    public static final String ADD_GROUP_TITLE = "add_group_title";

    public static final String ADD_SCRIPT_TITLE = "add_script_title";

    protected Dict()
    {
        // Can not be instantiated.
    }

    public static final String APPLICATION_NAME = "applicationName";

    public static final String OPENBIS_INSTANCE = "openbis_instance";

    public static final String FOOTER = "footer";

    //
    // Common Labels
    //

    public static final String ATTACHMENT = "attachment";

    public static final String CODE = "code";

    public static final String POSITION_AFTER = "position_after";

    public static final String ORDINAL = "ordinal";

    public static final String SECTION = "section";

    public static final String FILE = "file";

    public static final String PERM_ID = "perm_id";

    public static final String LOCATOR = "locator";

    public static final String REGISTRATOR = "registrator";

    public static final String REGISTRATION_DATE = "registration_date";

    public static final String NOT_IMPLEMENTED = "not_implemented";

    public static final String LOAD_IN_PROGRESS = "load_in_progress";

    public static final String LOAD_REST_OF_DATA = "load_rest_of_data";

    public static final String TABLE_OPERATIONS = "table_operations";

    public static final String ENTITY_OPERATIONS = "entity_operations";

    //
    // Field
    //

    public static final String COMBO_BOX_EMPTY = "combobox_empty";

    public static final String COMBO_BOX_CHOOSE = "combobox_choose";

    public static final String INVALID_CODE_MESSAGE = "invalid_code_message";

    //
    // MessageBox
    //

    public static final String MESSAGEBOX_ERROR = "messagebox_error";

    public static final String MESSAGEBOX_WARNING = "messagebox_warning";

    public static final String MESSAGEBOX_INFO = "messagebox_info";

    //
    // Buttons
    //

    public static final String TOOLTIP_REFRESH_ENABLED = "tooltip_refresh_enabled";

    public static final String TOOLTIP_REFRESH_DISABLED = "tooltip_refresh_disabled";

    public static final String TOOLTIP_EXPORT_ENABLED = "tooltip_export_enabled";

    public static final String TOOLTIP_EXPORT_DISABLED = "tooltip_export_disabled";

    public static final String TOOLTIP_CONFIG_ENABLED = "tooltip_config_enabled";

    public static final String TOOLTIP_CONFIG_DISABLED = "tooltip_config_disabled";

    public static final String TOOLTIP_VOCABULARY_MANAGED_INTERNALLY =
            "tooltip_vocabulary_managed_internally";

    public static final String TOOLTIP_VIEW_DATASET = "tooltip_view_dataset";

    public static final String BUTTON_ADD = "button_add";

    public static final String BUTTON_SAVE = "button_save";

    public static final String BUTTON_SAVE_AND_UPLOAD = "button_save_and_upload";

    public static final String BUTTON_CHOOSE = "button_choose";

    public static final String BUTTON_CANCEL = "button_cancel";

    public static final String BUTTON_RESET = "button_reset";

    public static final String BUTTON_SUBMIT = "button_submit";

    public static final String BUTTON_REFRESH = "button_refresh";

    public static final String BUTTON_SHOW = "button_show";

    public static final String BUTTON_BROWSE = "button_browse";

    public static final String BUTTON_VIEW = "button_view";

    public static final String BUTTON_SHOW_DETAILS = "button_show_details";

    public static final String BUTTON_EXPORT_TABLE = "button_exportTable";

    public static final String BUTTON_LOGOUT_LABEL = "logout_buttonLabel";

    public static final String BUTTON_HOME_LABEL = "home_buttonLabel";

    public static final String EXPORT_ALL_COLUMNS = "export_all_columns";

    public static final String EXPORT_VISIBLE_COLUMNS = "export_visible_columns";

    public static final String TOOLTIP_EXPORT_ALL_COLUMNS = "tooltip_export_all_columns";

    public static final String TOOLTIP_EXPORT_VISIBLE_COLUMNS = "tooltip_export_visible_columns";

    //
    // LoginWidget
    //

    public static final String LOGIN_INVITATION = "login_invitation";

    public static final String LOGIN_USER_LABEL = "login_userLabel";

    public static final String LOGIN_PASSWORD_LABEL = "login_passwordLabel";

    public static final String LOGIN_BUTTON_LABEL = "login_buttonLabel";

    public static final String LOGIN_FAILED = "login_failed";

    //
    // AbstractAsyncCallback
    //

    public static final String EXCEPTION_INVOCATION_MESSAGE = "exception_invocationMessage";

    public static final String EXCEPTION_WITHOUT_MESSAGE = "exception_withoutMessage";

    //
    // Header
    //

    public static final String HEADER_USER_WITHOUT_HOMEGROUP = "header_userWithoutHomegroup";

    public static final String HEADER_USER_WITH_HOMEGROUP = "header_userWithHomegroup";

    //
    // Search
    //

    public static final String SEARCH_BUTTON = "search_button";

    public static final String GLOBAL_SEARCH = "global_search";

    public static final String IDENTIFIER = "identifier";

    public static final String SAMPLE_PROPERTIES_PANEL_SAMPLE_IDENTIFIER =
            "sample_properties_panel_sample_identifier";

    public static final String NO_MATCH = "no_match";

    public static final String ENTITY_TYPE = "entity_type";

    public static final String ENTITY_KIND = "entity_kind";

    public static final String MATCHING_TEXT = "matching_text";

    public static final String MATCHING_FIELD = "matching_field";

    public static final String TOO_GENERIC = "too_generic";

    //
    // Sample Browser
    //

    public static final String SUBCODE = "subcode";

    public static final String SAMPLE = "sample";

    public static final String SAMPLE_TYPE = "sample_type";

    public static final String DATABASE_INSTANCE = "database_instance";

    public static final String SAMPLE_IDENTIFIER = "sample_identifier";

    public static final String IS_INSTANCE_SAMPLE = "is_instance_sample";

    public static final String IS_INVALID = "is_invalid";

    public static final String GROUP = "group";

    public static final String SPACE = "space";

    public static final String GROUPS = "groups";

    public static final String PROJECT = "project";

    public static final String EXPERIMENT = "experiment";

    public static final String EXPERIMENT_IDENTIFIER = "experiment_identifier";

    public static final String PART_OF = "part_of";

    //
    // Experiment Browser
    //

    public static final String EXPEIRMENTS_GRID_HEADER = "experiments_grid_header";

    public static final String EXPERIMENT_TYPE = "experiment_type";

    public static final String PROJECT_SELECTOR_TITLE = "project_selector_title";

    public static final String PROJECT_SELECTOR_TOOLTIP = "project_selector_tooltip";

    public static final String PROJECT_SELECTOR_DESCRIPTION_NOT_AVAILABLE =
            "project_selector_description_not_available";

    public static final String PROJECT_SELECTOR_CODE_COLUMN = "project_selector_code_column";

    public static final String PROJECT_SELECTOR_DETAILS_LINK_LABEL =
            "project_selector_details_link_label";

    public static final String PROJECT_SELECTOR_DETAILS_LINK_TOOLTIP =
            "project_selector_details_link_tooltip";

    //
    // Dataset Browser
    //

    public static final String CHILDREN_DATASETS = "children_datasets";

    public static final String NO_DATASETS_SELECTED = "no_datasets_selected";

    public static final String DATASETS_FROM_DIFFERENT_STORES_SELECTED =
            "datasets_from_different_stores_selected";

    public static final String PERFORM_COMPUTATION_ON_ALL_DATASETS_MSG_TEMPLATE =
            "perform_computation_on_all_datasets_msg_template";

    public static final String PERFORM_COMPUTATION_ON_SELECTED_OR_ALL_DATASETS_MSG_TEMPLATE =
            "perform_computation_on_selected_or_all_datasets_msg_template";

    public static final String PERFORM_ARCHIVING_ON_ALL_DATASETS_MSG_TEMPLATE =
            "perform_archiving_on_all_datasets_msg_template";

    public static final String PERFORM_ARCHIVING_ON_SELECTED_OR_ALL_DATASETS_MSG_TEMPLATE =
            "perform_archiving_on_selected_or_all_datasets_msg_template";

    //
    // Entity Type Browser
    //

    public static final String ADD_NEW_TYPE_BUTTON = "add_new_type_button";

    public static final String EDIT_TYPE_BUTTON = "edit_type_button";

    public static final String ADD_TYPE_TITLE_TEMPLATE = "add_type_title_template";

    public static final String EDIT_TYPE_TITLE_TEMPLATE = "edit_type_title_template";

    //
    // Sample Type Browser
    //

    public static final String SUBCODE_UNIQUE_LABEL = "subcode_unique_label";

    public static final String AUTO_GENERATE_CODES_LABEL = "auto_generate_codes_label";

    public static final String GENERATED_CODE_PREFIX = "generated_code_prefix";

    public static final String LISTABLE = "listable";

    public static final String IS_LISTABLE = "is_listable";

    public static final String SHOW_CONTAINER = "show_container";

    public static final String SHOW_PARENTS = "show_parents";

    public static final String IS_SHOW_CONTAINER = "is_show_container";

    public static final String IS_SHOW_PARENTS = "is_show_parents";

    //
    // Property Type Browser
    //

    public static final String LABEL = "label";

    public static final String DATA_TYPE = "data_type";

    public static final String DATA_TYPE_CODE = "data_type_code";

    public static final String DESCRIPTION = "description";

    public static final String SAMPLE_TYPES = "sample_types";

    public static final String MATERIAL_TYPES = "material_types";

    public static final String DATA_SET_TYPES = "data_set_types";

    public static final String FILE_FORMAT_TYPES = "file_format_types";

    public static final String EXPERIMENT_TYPES = "experiment_types";

    public static final String IS_MANDATORY = "is_mandatory";

    public static final String IS_DYNAMIC = "is_dynamic";

    public static final String IS_MANAGED = "is_managed";

    public static final String SCRIPT = "script";

    public static final String SCRIPT_REGISTRATION = "script_registration";

    public static final String PROPERTY_TYPE = "property_type";

    public static final String PROPERTY_TYPE_CODE = "property_type_code";

    public static final String ASSIGNED_TO = "assigned_to";

    public static final String TYPE_OF = "type_of";

    public static final String VOCABULARY = "vocabulary";

    public static final String VOCABULARY_TERMS = "vocabulary_terms";

    public static final String VOCABULARY_TERMS_FILE_FORMAT = "vocabulary_terms_file_format";

    public static final String VOCABULARY_TERMS_URL_TEMPLATE = "vocabulary_terms_url_template";

    public static final String VOCABULARY_TERMS_EMPTY = "vocabulary_terms_empty";

    public static final String MISSING_VOCABULARY_TERMS = "missing_vocabulary_terms";

    public static final String VOCABULARY_SHOW_AVAILABLE_TERMS_IN_CHOOSERS =
            "vocabulary_show_available_terms_in_choosers";

    public static final String CONFIRM_VOCABULARY_SHOW_AVAILABLE_TERMS_IN_CHOOSERS_MSG =
            "confirm_vocabulary_show_available_terms_in_chooosers_msg";

    public static final String MANDATORY = "mandatory";

    public static final String DYNAMIC = "dynamic";

    public static final String MANAGED = "managed";

    public static final String SCRIPTABLE = "scriptable";

    public static final String SECTION_TOOLTIP = "section_tooltip";

    public static final String DEFAULT_VALUE = "default_value";

    public static final String DEFAULT_VALUE_TOOLTIP = "default_value_tooltip";

    public static final String DEFAULT_UPDATE_VALUE = "default_update_value";

    public static final String DEFAULT_UPDATE_VALUE_TOOLTIP = "default_update_value_tooltip";

    public static final String XML_SCHEMA = "xml_schema";

    public static final String XML_SCHEMA_INFO = "xml_schema_info";

    public static final String XSLT = "xslt";

    public static final String XSLT_INFO = "xslt_info";

    //
    // Property Type Assignment Browser
    //

    public static final String UNASSIGN_BUTTON_LABEL = "unassign_button_label";

    public static final String UNASSIGNMENT_CONFIRMATION_DIALOG_TITLE =
            "unassignment_confirmation_dialog_title";

    public static final String UNASSIGNMENT_CONFIRMATION_TEMPLATE_WITHOUT_PROPERTIES =
            "unassignment_confirmation_template_without_properties";

    public static final String UNASSIGNMENT_CONFIRMATION_TEMPLATE_WITH_PROPERTIES =
            "unassignment_confirmation_template_with_properties";

    public static final String EDIT_PROPERTY_TYPE_ASSIGNMENT_TITLE =
            "edit_property_type_assignment_title";

    //
    // Attachments Browser
    //

    public static final String ADD_ATTACHMENT_TITLE = "add_attachment_title";

    public static final String ATTACHMENTS = "attachments";

    public static final String FILE_NAME = "file_name";

    public static final String TITLE = "title";

    public static final String VERSION_FILE_NAME = "version_file_name";

    public static final String VERSION = "version";

    public static final String SHOW_ALL_VERSIONS = "show_all_versions";

    public static final String BUTTON_DOWNLOAD = "button_download";

    public static final String BUTTON_SHOW_ALL_VERSIONS = "button_show_all_versions";

    public static final String NO_ATTACHMENTS_FOUND = "no_attachments_found";

    public static final String NO_SAMPLES_FOUND = "no_samples_found";

    // -------- generic plugin dictionary -------------------

    //
    // Sample Viewer
    //

    public static final String SAMPLE_PROPERTIES_HEADING = "sample_properties_heading";

    public static final String PART_OF_HEADING = "part_of_heading";

    public static final String DERIVED_SAMPLES_HEADING = "derived_samples_heading";

    public static final String PARENT_SAMPLES_HEADING = "parent_samples_heading";

    public static final String DERIVED_SAMPLE = "derived_sample";

    public static final String EXTERNAL_DATA_HEADING = "external_data_heading";

    public static final String INVALIDATION = "invalidation";

    public static final String INVALIDATION_TEMPLATE = "invalidation_template";

    public static final String SHOW_ONLY_DIRECTLY_CONNECTED = "show_only_directly_connected";

    //
    // ExternalData Viewer
    //

    public static final String DATA_SET_PROPERTIES_HEADING = "data_set_properties_heading";

    public static final String PARENT = "parent";

    public static final String PARENT_CODE = "parent_code";

    public static final String LOCATION = "location";

    public static final String FILE_FORMAT_TYPE = "file_format_type";

    public static final String DATA_SET_TYPE = "data_set_type";

    public static final String EXTERNAL_DATA_EXPERIMENT_IDENTIFIER =
            "external_data_experiment_identifier";

    public static final String EXTERNAL_DATA_SAMPLE_IDENTIFIER = "external_data_sample_identifier";

    public static final String SOURCE_TYPE = "source_type";

    public static final String COMPLETE = "complete";

    public static final String IS_COMPLETE = "is_complete";

    public static final String ARCHIVING_STATUS = "archiving_status";

    public static final String PRODUCTION_DATE = "production_date";

    public static final String DATA_PRODUCER_CODE = "data_producer_code";

    public static final String DATA_STORE_CODE = "data_store_code";

    public static final String BUTTON_UPLOAD_DATASETS = "button_upload_datasets";

    public static final String BUTTON_PROCESS = "button_process";

    public static final String PROCESSING_INFO_TITLE = "processing_info_title";

    public static final String PROCESSING_INFO_MSG = "processing_info_msg";

    public static final String CONFIRM_DATASET_UPLOAD_TITLE = "confirm_dataset_upload_title";

    public static final String CONFIRM_DATASET_UPLOAD_MSG = "confirm_dataset_upload_msg";

    public static final String CONFIRM_DATASET_UPLOAD_FILE_NAME_FIELD =
            "confirm_dataset_upload_file_name_field";

    public static final String CONFIRM_DATASET_UPLOAD_COMMENT_FIELD =
            "confirm_dataset_upload_comment_field";

    public static final String CONFIRM_DATASET_UPLOAD_USER_FIELD =
            "confirm_dataset_upload_user_field";

    public static final String CONFIRM_DATASET_UPLOAD_PASSWORD_FIELD =
            "confirm_dataset_upload_password_field";

    public static final String DATASET_NOT_AVAILABLE_MSG = "dataset_not_available_msg";

    //
    // Sample Registration
    //

    public static final String INSTANCE_SAMPLE = "instance_sample";

    public static final String GENERATED_FROM_SAMPLE = "generated_from_sample";

    public static final String ADD_PARENT = "add_parent";

    public static final String PART_OF_SAMPLE = "part_of_sample";

    //
    // Menu Titles
    //

    public static final String MENU_ADMINISTRATION = "menu_administration";

    public static final String MENU_AUTHORIZATION = "menu_authorization";

    public static final String MENU_PROJECT = "menu_project";

    public static final String MENU_PROPERTY_TYPES = "menu_property_types";

    public static final String MENU_VOCABULARY = "menu_vocabulary";

    public static final String MENU_SCRIPT = "menu_script";

    public static final String MENU_DATA_SET = "menu_data_set";

    public static final String MENU_EXPERIMENT = "menu_experiment";

    public static final String MENU_MATERIAL = "menu_material";

    public static final String MENU_SAMPLE = "menu_sample";

    public static final String MENU_MODULES = "menu_modules";

    public static final String MENU_COMPUTE = "menu_compute";

    public static final String MENU_PROCESSING = "menu_processing";

    public static final String MENU_ARCHIVING = "menu_archiving";

    //
    // Tab Titles
    //

    public static final String ASSIGN_EXPERIMENT_PROPERTY_TYPE = "assign_experiment_property_type";

    public static final String ASSIGN_MATERIAL_PROPERTY_TYPE = "assign_material_property_type";

    public static final String ASSIGN_DATA_SET_PROPERTY_TYPE = "assign_data_set_property_type";

    public static final String ASSIGN_SAMPLE_PROPERTY_TYPE = "assign_sample_property_type";

    public static final String PROPERTY_TYPE_ASSIGNMENTS = "property_type_assignments";

    public static final String PROPERTY_TYPE_REGISTRATION = "property_type_registration";

    public static final String PROPERTY_TYPES = "property_types";

    public static final String EXPERIMENT_BROWSER = "experiment_browser";

    public static final String VOCABULARY_REGISTRATION = "vocabulary_registration";

    public static final String SAMPLE_BATCH_REGISTRATION = "sample_batch_registration";

    public static final String SAMPLE_BATCH_UPDATE = "sample_batch_update";

    public static final String MATERIAL_BATCH_UPDATE = "material_batch_update";

    public static final String DATA_SET_BATCH_UPDATE = "data_set_batch_update";

    public static final String SAMPLE_REGISTRATION = "sample_registration";

    public static final String SAMPLE_BROWSER = "sample_broser";

    public static final String LIST_GROUPS = "list_groups";

    public static final String CONFIRM_TITLE = "confirm_title";

    public static final String CONFIRM_CLOSE_MSG = "confirm_close_msg";

    //
    // Role View
    //
    public static final String ROLE = "role";

    public static final String CONFIRM_ROLE_REMOVAL_MSG = "confirm_role_removal_msg";

    public static final String CONFIRM_ROLE_REMOVAL_TITLE = "confirm_role_removal_title";

    //
    // Experiment Registration/Update
    //

    public static final String EXPERIMENT_REGISTRATION = "experiment_registration";

    public static final String SAMPLES = "samples";

    public static final String SAMPLES_LIST = "samples_list";

    public static final String EXPERIMENT_IMPORT = "experiment_import";

    public static final String EXPERIMENT_BATCH_UPDATE = "experiment_batch_update";

    //
    // Data Set Edition
    //

    public static final String PARENTS = "parents";

    public static final String PARENTS_EMPTY = "parents_empty";

    //
    // Vocabulary Browser
    //
    public static final String VOCABULARY_BROWSER = "vocabulary_browser";

    public static final String IS_MANAGED_INTERNALLY = "is_managed_internally";

    public static final String URL_TEMPLATE = "url_template";

    public static final String TERMS = "terms";

    public static final String VOCABULARY_TERMS_BROWSER = "VOCABULARY_TERMS_BROWSER";

    public static final String TERM_FOR_SAMPLES_USAGE = "TERM_FOR_SAMPLES_USAGE";

    public static final String TERM_FOR_DATA_SET_USAGE = "TERM_FOR_DATA_SET_USAGE";

    public static final String TERM_FOR_EXPERIMENTS_USAGE = "TERM_FOR_EXPERIMENTS_USAGE";

    public static final String TERM_FOR_MATERIALS_USAGE = "TERM_FOR_MATERIALS_USAGE";

    public static final String TERM_TOTAL_USAGE = "TERM_TOTAL_USAGE";

    public static final String ADD_VOCABULARY_TERMS_BUTTON = "add_vocabulary_terms_button";

    public static final String ADD_VOCABULARY_TERMS_TITLE = "add_vocabulary_terms_title";

    public static final String ADD_VOCABULARY_TERMS_OK_BUTTON = "add_vocabulary_terms_ok_button";

    public static final String UPDATE_VOCABULARY_TERMS_BUTTON = "update_vocabulary_terms_button";

    public static final String UPDATE_VOCABULARY_TERMS_TITLE = "update_vocabulary_terms_title";

    public static final String UPDATE_VOCABULARY_TERMS_MESSAGE = "update_vocabulary_terms_message";

    public static final String UPDATE_VOCABULARY_TERMS_MESSAGE_2 =
            "update_vocabulary_terms_message_2";

    public static final String VOCABULARY_TERMS_VALIDATION_MESSAGE =
            "vocabulary_terms_validation_message";

    public static final String DELETE_VOCABULARY_TERMS_BUTTON = "delete_vocabulary_terms_button";

    public static final String DELETE_VOCABULARY_TERMS_INVALID_TITLE =
            "delete_vocabulary_terms_invalid_title";

    public static final String DELETE_VOCABULARY_TERMS_INVALID_MESSAGE =
            "delete_vocabulary_terms_invalid_message";

    public static final String DELETE_VOCABULARY_TERMS_CONFIRMATION_TITLE =
            "delete_vocabulary_terms_confirmation_title";

    public static final String DELETE_VOCABULARY_TERMS_CONFIRMATION_MESSAGE_NO_REPLACEMENTS_SINGULAR =
            "delete_vocabulary_terms_confirmation_message_no_replacements_singular";

    public static final String DELETE_VOCABULARY_TERMS_CONFIRMATION_MESSAGE_NO_REPLACEMENTS =
            "delete_vocabulary_terms_confirmation_message_no_replacements";

    public static final String DELETE_VOCABULARY_TERMS_CONFIRMATION_MESSAGE_FOR_REPLACEMENTS =
            "delete_vocabulary_terms_confirmation_message_for_replacements";

    public static final String EDIT_VOCABULARY_TERM_BUTTON = "edit_vocabulary_term_button";

    //
    // Group Browser
    //
    public static final String GROUP_BROWSER = "group_browser";

    //
    // Person Browser
    //
    public static final String PERSON_BROWSER = "person_browser";

    public static final String USER_ID = "user_id";

    public static final String FIRST_NAME = "first_name";

    public static final String LAST_NAME = "last_name";

    public static final String EMAIL = "email";

    //
    // Role Browser
    //
    public static final String ROLE_ASSIGNMENT_BROWSER = "role_assignment_browser";

    public static final String PERSON = "person";

    public static final String BUTTON_ASSIGN_ROLE = "button_assign_role";

    public static final String BUTTON_RELEASE_ROLE_ASSIGNMENT = "button_release_role_assignment";

    //
    // Project Browser
    //
    public static final String PROJECT_BROWSER = "project_browser";

    //
    // Project Registration
    //
    public static final String PROJECT_REGISTRATION = "project_registration";

    //
    // Detailed Search
    //
    public static final String DATA_SET_SEARCH = "data_set_search";

    public static final String SAMPLE_SEARCH = "sample_search";

    public static final String MATCH_ALL = "match_all";

    public static final String MATCH_ANY = "match_any";

    public static final String BUTTON_CHANGE_QUERY = "button_change_query";

    //
    // Material Browser
    //
    public static final String MATERIAL_TYPE = "material_type";

    public static final String MATERIAL_BROWSER = "material_browser";

    public static final String INHIBITOR_OF = "infibitor_of";

    public static final String ALLOW_ANY_TYPE = "allow_any_type";

    //
    // Material Import
    //
    public static final String MATERIAL_IMPORT = "material_import";

    public static final String IGNORE_UNREGISTERED_MATERIALS = "ignore_unregistered_materials";

    //
    // Script Browser
    //
    public static final String SCRIPT_BROWSER = "script_browser";

    public static final String SCRIPT_TYPE = "script_type";

    //
    // Material Chooser
    //

    public static final String TITLE_CHOOSE_MATERIAL = "title_choose_material";

    public static final String TITLE_CHOOSE_SCRIPT = "title_choose_script";

    public static final String CHOOSE_ANY_MATERIAL = "choose_any_material";

    public static final String INCORRECT_MATERIAL_SYNTAX = "incorrect_material_syntax";

    public static final String TITLE_CHOOSE_EXPERIMENT = "TITLE_CHOOSE_EXPERIMENT";

    public static final String INCORRECT_EXPERIMENT_SYNTAX = "incorrect_experiment_syntax";

    public static final String TITLE_CHOOSE_SAMPLE = "title_choose_sample";

    //
    // Grid Column Chooser
    //

    public static final String GRID_SETTINGS_TITLE = "grid_settings_title";

    public static final String GRID_COLUMN_NAME_HEADER = "GRID_COLUMN_NAME_HEADER";

    public static final String GRID_IS_COLUMN_VISIBLE_HEADER = "GRID_IS_COLUMN_VISIBLE_HEADER";

    public static final String GRID_COLUMN_HAS_FILTER_HEADER = "GRID_COLUMN_HAS_FILTER_HEADER";

    public static final String TOO_MANY_VISIBLE_COLUMNS_TITLE = "TOO_MANY_VISIBLE_COLUMNS_TITLE";

    public static final String TOO_MANY_VISIBLE_COLUMNS_MSG = "TOO_MANY_VISIBLE_COLUMNS_MSG";

    public static final String VISIBLE_COLUMNS_LIMIT_EXCEEDED_MSG =
            "VISIBLE_COLUMNS_LIMIT_EXCEEDED_MSG";

    public static final String VISIBLE_COLUMNS_LIMIT_REACHED_MSG =
            "VISIBLE_COLUMNS_LIMIT_REACHED_MSG";

    //
    // User Settings Dialog
    //

    public static final String HOME_GROUP_LABEL = "home_group_label";

    public static final String CHANGE_USER_SETTINGS_DIALOG_TITLE =
            "change_user_settings_dialog_title";

    public static final String USE_WILDCARD_SEARCH_MODE_LABEL = "use_wildcard_search_mode_label";

    public static final String USE_WILDCARD_SEARCH_MODE_TOOLTIP =
            "use_wildcard_search_mode_tooltip";

    public static final String RESET_USER_SETTINGS_BUTTON = "reset_user_settings_button";

    public static final String RESET_USER_SETTINGS_CONFIRMATION_MSG =
            "reset_user_settings_confirmation_msg";

    public static final String REAL_NUMBER_FORMATING_FIELDS = "real_number_formating_fields";

    public static final String SCIENTIFIC_FORMATING = "scientific_formating";

    public static final String REAL_NUMBER_FORMATING_PRECISION = "real_number_formating_precision";

    public static final String DEBUGGING_MODE = "debugging_mode";

    public static final String DEBUGGING_MODE_INFO = "debugging_mode_info";

    public static final String REOPEN_LAST_TAB_ON_LOGIN_LABEL = "reopen_last_tab_on_login_label";

    public static final String REOPEN_LAST_TAB_ON_LOGIN_INFO = "reopen_last_tab_on_login_info";

    //
    // Help Info
    //

    public static final String INFO_BUTTON_TOOLTIP = "info_button_tooltip";

    public static final String INFO_BOX_TITLE = "info_box_title";

    public static final String INFO_BOX_MSG = "info_box_msg";

    //
    // Unclassified
    //

    public static final String MATERIAL = "material";

    public static final String DATA_SET = "data_set";

    public static final String FILTER = "filter";

    public static final String FILTERS = "filters";

    public static final String ENTITY_TYPE_ASSIGNMENTS = "entity_type_assignments";

    public static final String COMBO_BOX_EXPECTED_VALUE_FROM_THE_LIST =
            "combo_box_expected_value_from_the_list";

    public static final String DETAILS_TITLE = "details_title";

    public static final String EDIT_TITLE = "edit_title";

    public static final String BUTTON_EDIT = "edit";

    public static final String BUTTON_DELETE = "button_delete";

    public static final String DELETE_CONFIRMATION_TITLE = "delete_confirmation_title";

    public static final String DELETE_CONFIRMATION_MESSAGE = "delete_confirmation_message";

    public static final String DELETE_CONFIRMATION_MESSAGE_WITH_REASON =
            "delete_confirmation_message_with_reason";

    public static final String DELETE_PROGRESS_MESSAGE = "delete_progress_message";

    public static final String DELETE_CONFIRMATION_WARNING = "delete_confirmation_warning";

    public static final String DELETE_CONFIRMATION_WARNING_PART_FOR_EXPERIMENT =
            "delete_confirmation_warning_part_for_experiment";

    public static final String DELETE_CONFIRMATION_WARNING_PART_FOR_SAMPLE =
            "delete_confirmation_warning_part_for_sample";

    public static final String BUTTON_CONFIGURE = "button_configure";

    public static final String BUTTON_FILTERS = "button_filters";

    public static final String TAB_LINK = "tab_link";

    public static final String TAB_LINK_MESSAGE = "tab_link_message";

    public static final String PERMLINK = "permlink_column_name";

    public static final String SHOW_DETAILS_LINK = "show_details_link_column_name";

    public static final String SHOW_DETAILS_LINK_TEXT_VALUE = "show_details_link_text_value";

    public static final String OVERVIEW = "overview_column_name";

    public static final String FILE_TEMPLATE_LABEL = "file_template_label";

    public static final String URL = "url";

    public static final String REASON = "reason";

    public static final String ADD_ATTACHMENT = "add_attachment";

    public static final String EXPERIMENT_SAMPLES_SELCTION_TITLE =
            "experiment_samples_selction_title";

    public static final String DATA_SET_UPLOAD = "data_set_upload";

    public static final String BUTTON_UPLOAD_DATA_VIA_CIFEX = "button_upload_data_via_cifex";

    public static final String BUTTON_SHOW_RELATED_DATASETS = "show_related_datasets";

    public static final String SHOW_RELATED_DATASETS_DIALOG_TITLE = "show_related_datasets";

    public static final String SHOW_RELATED_DATASETS_DIALOG_MESSAGE =
            "show_related_datasets_message";

    public static final String SHOW_RELATED_DATASETS_DIALOG_RADIO_LABEL =
            "show_related_datasets_radio_label";

    public static final String MESSAGE_NO_EXTERNAL_UPLOAD_SERVICE =
            "message_no_external_upload_service";

    public static final String DATA_STORE = "data_store";

    public static final String AUTHORIZATION_GROUP = "authorization_group";

    public static final String AUTHORIZATION_GROUP_BROWSER = "authorization_group_browser";

    public static final String EDIT_PERSONS = "edit_persons";

    public static final String ADD_PERSON_TO_AUTHORIZATION_GROUP_TITLE =
            "add_person_to_authorization_group_title";

    public static final String PERSONS_IDS_LABEL = "persons_ids_label";

    public static final String PERSON_IDS_LIST = "person_ids_list";

    public static final String AUTHORIZATION_GROUP_USERS = "authorization_group_users";

    public static final String BUTTON_SHOW_USERS = "button_show_users";

    public static final String RADIO_ONE_USER = "radio_one_user";

    public static final String RADIO_MANY_USERS = "radio_many_users";

    public static final String REMOVE_PERSONS_FROM_AUTHORIZATION_GROUP_CONFIRMATION_TITLE =
            "remove_persons_from_authorization_group_confirmation_title";

    public static final String REMOVE_PERSONS_FROM_AUTHORIZATION_GROUP_CONFIRMATION_MESSAGE =
            "remove_persons_from_authorization_group_confirmation_message";

    public static final String ALL_RADIO = "all_radio";

    public static final String DATA_SETS_RADIO_GROUP_LABEL = "data_sets_radio_group_label";

    public static final String ONLY_SELECTED_RADIO = "only_selected_radio";

    public static final String EXPERIMENTS_RADIO_GROUP_LABEL = "experiments_radio_group_label";

    public static final String SAMPLES_RADIO_GROUP_LABEL = "samples_radio_group_label";

    public static final String NAME = "name";

    public static final String IS_PUBLIC = "is_public";

    public static final String EXPRESSION = "expression";

    public static final String COLUMN = "column";

    public static final String COLUMNS = "columns";

    public static final String GRID_CUSTOM_FILTERS = "grid_custom_filters";

    public static final String GRID_CUSTOM_COLUMNS = "grid_custom_columns";

    public static final String APPLY_FILTER = "apply_filter";

    public static final String RESET_FILTER = "reset_filter";

    public static final String ADD_NEW_FILTER = "add_new_filter";

    public static final String ADD_NEW_COLUMN = "add_new_column";

    public static final String HOW_TO_ADDRESS = "how_to_address";

    public static final String INSERT_COLUMNS = "insert_columns";

    public static final String OPENBIS_LOGO_TITLE = "openbis_logo_title";

    public static final String MATERIALS_RADIO_GROUP_LABEL = "materials_radio_group_label";

    public static final String DATA_VIEW = "data_view";

    public static final String MAIN_DATA_SET_PATH = "main_data_set_path";

    public static final String MAIN_DATA_SET_PATH_TOOLTIP = "main_data_set_path_tooltip";

    public static final String MAIN_DATA_SET_PATH_EXAMPLE = "main_data_set_path_example";

    public static final String MAIN_DATA_SET_PATTERN = "main_data_set_pattern";

    public static final String MAIN_DATA_SET_PATTERN_TOOLTIP = "main_data_set_pattern_tooltip";

    public static final String MAIN_DATA_SET_PATTERN_EXAMPLE = "main_data_set_pattern_example";

    public static final String AUTO_RESOLVE_LABEL = "auto_resolve_label";

    public static final String DATA_REPORT_LABEL = "data_report_label";

    public static final String BUTTON_TOP = "button_top";

    public static final String EXPLORE_LABEL = "explore_label";

    public static final String DEFAULT_GROUP = "default_group";

    public static final String LOGGING_CONSOLE = "LOGGING_CONSOLE";

    public static final String MORE_RESULTS_FOUND_MESSAGE = "more_results_found_message";

    public static final String MORE_RESULTS_FOUND_TITLE = "more_results_found_title";

    // Material Viewer

    public static final String MATERIAL_PROPERTIES_HEADING = "material_properties_heading";

    public static final String BUTTON_EVALUATE = "button_evaluate";

    public static final String EVALUATION_RESULT = "evaluation_result";

    public static final String SCRIPT_TESTER = "script_tester";

    public static final String ENTITY_DETAILS = "entity_details";

    public static final String SHOW_DETAILS = "show_details";

    public static final String EVALUATION_IN_PROGRESS = "evaluation_in_progress";

    public static final String WARNING_NO_SCRIPT_TITLE = "warning_no_script_title";

    public static final String WARNING_NO_SCRIPT_MESSAGE = "warning_no_script_message";

    // ----- end generic ------------------
}
