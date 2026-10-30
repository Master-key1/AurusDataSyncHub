package com.auruspay.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Constants {

    public static enum DATASOURCE {
        MARIADB,POSTGRES,MARIADB_POSTGRES,MARIADB_PRIMARY,MARIADB_SECONDARY,POSTGRES_PRIMARY,POSTGRES_SECONDARY,
        PRIMARY_BACKOFFICE,SECONDARY_BACKOFFICE,POSTGRES_PRIMARY_STOREPLACE,VPN_PRIMARY_STOREPLACE,POSTGRES_SECONDARY_STOREPLACE,VPN_SECONDARY_STOREPLACE

    }

    public static enum DATABASE {
        MARIADB, POSTGRES
    }

    public static enum PROCESSED_ISO {
        PROCESSED_REQUEST, PROCESSED_RESPONSE
    }

    public static final String HOTROD_SERVER_LIST = "infinispan.client.hotrod.server_list";
    public static final String AURUS_LOCAL_HOST = "127.0"+".0.1";

    public static final long SPACE_DEFAULT_OUT_EXPIRE_TIME = 10000;

    public static final String PCI_APP_VERSION = "AurusPay v5.1.5.31";

    // Credit card types
    public static final String VISA = "VI";
    public static final String MASTERCARD = "MC";
    public static final String AMEX = "AX";
    public static final String DISCOVER = "NV";
    public static final String DINER = "DC";
    public static final String JCB = "JB";
    public static final String LOYALTY_TYPE = "VL";

    public static final char DECLINED_TXNID_IDENTIFIER = '2';
    public static final char APPROVED_TXNID_IDENTIFIER = '1';

    public static final int MASTERCARD_DIGIT = 5;
    public static final int VISA_DIGIT = 4;
    public static final int DISCOVER_DIGIT = 6;
    public static final int AMEX_DIGIT = 37;
    public static final int DINER_DIGIT = 36;

    public static final String DB = "DB";
    public static final String GC = "GC";
    public static final String PLC = "PL";
    public static final String NMG_EMP = "EM";
    public static final String XX = "XX";
    public static final String CC_TYPE = "C";
    public static final String DB_TYPE = "D";
    public static final String GC_TYPE = "C";
    public static final String PLCC_TYPE = "C";
    public static final String EBT_TYPE = "E";
    public static final String PREPAID_TYPE = "P";
    public static final String FSA_TYPE = "S";
    public static final String HSA_TYPE = "H";
    public static final String FLEET_TYPE = "F";
    public static final String CHECK_TYPE = "CHK";
    public static final String XXX = "XXX";
    public static final String WEX = "WX";
    public static final String VOYAGER = "VG";
    public static final String INTERAC = "IN";
    public static final String EPP = "EPP";
    public static final String CBC = "CB";
    public static final String FLEET_ONE = "FO";
    public static final String FUELMAN   = "FW";

    // Card Types for PENSKE
    public static final String AAA = "AAA";

    // Card Types for ANN,LOFT
    public static final String ANN_PLCC = "AP";
    public static final String ANN_CBCC = "AC";
    public static final String LOFT_PLCC = "LP";
    public static final String LOFT_CBCC = "LC";
    public static final String CBCC = "CB";

    // Account Types
    public static final String DEFAULT   = "0";
    public static final String CHECKING  = "1";
    public static final String SAVING    = "2";
    public static final String CONSUMER  = "3";
    public static final String CORPORATE = "4";

    // Currency Types
    public static final String CURRENCY_US  = "840";
    public static final String CURRENCY_CAD = "124";
    public static final String CURRENCY_UK  = "826";
    public static final String CURRENCY_COLOMBIA    = "170";
    public static final String CURRENCY_MEXICO      = "484";
    public static final String CURRENCY_PERU        = "604";
    public static final String CURRENCY_UNIDAD_DE_FOMENTO    = "990";
    public static final String CURRENCY_CHILE       = "152";
    public static final String CURRENCY_BRAZIL      = "986";
    public static final String CURRENCY_ICELAND     = "352";
    public static final String CURRENCY_EUROPE      = "978";
    public static final String CURRENCY_POLAND      = "616";
    public static final String CURRENCY_NETHERLAND  = "528";
    public static final String CURRENCY_AUSTRIA     = "40";
    public static final String CURRENCY_SPAIN       = "724";
    public static final String CURRENCY_GERMANY     = "276";
    public static final String CURRENCY_FRANCE      = "250";
    public static final String CURRENCY_ITALY       = "380";
    public static final String CURRENCY_BELGIUM     = "56";
    public static final String CURRENCY_PORTUGAL    = "620";
    public static final String CURRENCY_SWEDEN      = "752";
    public static final String CURRENCY_INDIA       = "356";
    public static final String CURRENCY_ARGENTINA   = "032";
    public static final String CURRENCY_URUGUAY     = "858";

    // NUMBER CONSTANTS
    public static final int ZERO    = 0;
    public static final int ONE     = 1;
    public static final int TWO     = 2;
    public static final int THREE   = 3;
    public static final int FOUR    = 4;
    public static final int FIVE    = 5;
    public static final int SIX     = 6;
    public static final int SEVEN   = 7;
    public static final int EIGHT   = 8;
    public static final int NINE    = 9;
    public static final int TEN     = 10;
    public static final int TWELVE  = 12;
    public static final int FIFTEEN = 15;
    public static final int SIXTEEN = 16;
    public static final int SEVENTEEN   = 17;
    public static final int EIGHTEEN    = 18;
    public static final int NINETEEN    = 19;
    public static final int TWENTY      = 20;
    public static final int TWENTY_ONE  = 21;
    public static final int TWENTY_TWO  = 22;
    public static final int TWENTY_THREE= 23;
    public static final int TWENTY_FOUR = 24;
    public static final int TWENTY_FIVE = 25;
    public static final int THIRTY      = 30;
    public static final int THIRTY_TWO  = 32;
    public static final int THIRTY_THREE= 33;
    public static final int FOURTY      = 40;
    public static final int FOURTY_ONE    = 41;
    public static final int FOURTY_TWO    = 42;
    public static final int FOURTY_THREE  = 43;
    public static final int FOURTY_FOUR   = 44;
    public static final int FOURTY_FIVE   = 45;
    public static final int FOURTY_SIX    = 46;
    public static final int FOURTY_SEVEN  = 47;
    public static final int FOURTY_EIGHT  = 48;
    public static final int FOURTY_NINE   = 49;

    public static final int FIFTY       = 50;
    public static final int FIFTY_ONE     = 51;
    public static final int FIFTY_TWO     = 52;
    public static final int FIFTY_THREE   = 53;
    public static final int FIFTY_FOUR    = 54;
    public static final int FIFTY_FIVE    = 55;
    public static final int FIFTY_SEVEN   = 57;
    public static final int FIFTY_EIGHT   = 58;

    public static final int SEVENTY_NINE= 79;
    public static final int ONE_HUNDRED = 100;
    public static final int ONE_HUNDRED_ONE = 101;
    public static final int FIVE_HUNDRED = 500;
    public static final int FIFTEEN_HUNDRED = 1500;
    public static final int TW0_THOUSAND = 2000;
    public static final String TWO_HUNDRED = "200";
    public static final String TWO_HUNDRED_ONE = "201";
    public static final String FOUR_HUNDRED_ONE = "401";
    public static final String APPROVED_CODE = "000";
    public static final String NINE_DIGIT = "9";
    public static final String ZERO_ONE = "01";

    // LETTER
    public static final String LETTER_D = "D";

    // SPECIAL CHAR
    public static final String EQUALS = "=";

    // CGI SUB CARD
    public static final String CSD = "CSD";
    public static final String CSP = "CSP";

    public static final String DATA_FIELD_SEPARATOR = "%1E";
    public static final String DATA_RECORD_SEPARATOR = "%7E";
    public static final String DATA_TICKET_SEPARATOR = "%1D";
    public static final String DATA_VALUE_SEPARATOR = "%1F";
    public static final String LETTER_O = "O";
    public static final String LETTER_N = "N";
    public static final String LETTER_Y = "Y";

    // ERROR MESSAGE CONSTANT FOR REQUEST VALIDATION
    public static final String EMPTY_STRING = "";
    public static final String IS_REQUIRED_MSG = " IS REQUIRED";
    public static final String INVALID_MSG = " IS INVALID";
    public static final String FIXED_LENGTH_REQUIRED_MSG = " LENGTH SHOULD BE ";
    public static final String VARIABLE_LENGTH_REQUIRED_FIRST_MSG = " LENGTH SHOULD BE IN RANGE ";
    public static final String VARIABLE_LENGTH_REQUIRED_SECOND_MSG = " TO ";
    public static final String REQUEST_SENDING_FAILED = "Request sending failed at RequestAuthorizationParticipant: ";
    public static final String CLASS_NAME_PROFILER = "RequestAuthorizationParticipant";

    // JSON SCHEMA VALIDATION
    public static final String UNWANTED = "unwanted";

    // ADDED FOR KEY EXCHANGE
    public static final String KEY_REQUESTED = "1";
    public static final String KEY_NOT_REQUESTED = "0";
    public static final String KEY_ERROR = "2";
    public static final String KEY_EXCHANGE_SETTLE_STATUS = "1";

    // FOR RECEIPT UPLOAD [DB_FLAG]
    public static final String INSERT_FLAG = "I";
    public static final String DELETE_FLAG = "D";
    public static final String UPDATE_FLAG = "U";

    public static final String TOKEN_LOOKUP_FLAG ="5";
    public static final String CARD_IDENTIFIER_LOOK_UP ="1";
    public static final String MERCHANT_TOKEN_LOOK_UP ="2";
    public static final String PROCESSOR_TOKEN_LOOK_UP ="3";

    public static final int CARD_NO = 0;
    public static final int CARD_IDENTIFIER = 1;
    public static final int MERCHANT_TOKEN = 2;
    public static final int PROCESSOR_TOKEN = 3;
    public static final int ONE_TIME_TOKEN = 4;
    public static final int ONE_ORDER_TOKEN = 5;
    public static final List<Integer> ECOM_TOKEN_OTT = Arrays.asList(CARD_NO, CARD_IDENTIFIER, MERCHANT_TOKEN, PROCESSOR_TOKEN);
    public static final List<Integer> ECOM_TOKEN = Arrays.asList(CARD_IDENTIFIER, MERCHANT_TOKEN, PROCESSOR_TOKEN);

    // Encryption Flag
    public static final String HEX_ENCRYPTION_FLAG                   = "00";
    public static final String AES_ENCRYPTION_FLAG                   = "01";
    public static final String AES_DEFAULT_ENCRYPTION_FLAG           = "02";
    public static final String PLAIN_ENCRYPTION_FLAG                 = "03";
    public static final String VAULT_AES_ENCRYPTION_FLAG             = "04";
    public static final String AES_DEFAULT_WITH_ENC_ENCRYPTION_FLAG  = "05";
    public static final String AES_DYNAMIC_WITH_ENC_ENCRYPTION_FLAG  = "06";
    public static final String AES_DEFAULT_WITH_FULL_KEY_ENCRYPTION_FLAG       = "07";
    public static final String AES_DEFAULT_WITH_FULL_KEY_ENC_ENCRYPTION_FLAG   = "08";
    public static final String AES_DEFAULT_WITH_FULL_KEY_ENCRYPTION_FLAG_NEW       = "09";
    public static final String AES_DEFAULT_WITH_FULL_KEY_ENC_ENCRYPTION_FLAG_NEW   = "10";
    public static final String AES_CORPORATE_ENCRYPTION_FLAG               = "99";
    public static final String AES_CORPORATE_WITH_ENC_ENCRYPTION_FLAG      = "98";
    public static final String HEX_ENC_ENCRYPTION_FLAG                     = "97";

    public static final String AES_DEFAULT_FULL_KEY = "AyVp4Rl081InvIZ6rzY+Yi9BnaAa3588Bpor6n9rJCE=";
    //public static final String AES_DEFAULT_FULL_KEY = "8RLx6Na9Btqv/1R9u7zLTB9MGIVdfSjK1dqP2X798EA=";

    public static final List<String> AES_ENCRYPTION_LIST=Arrays.asList(AES_DEFAULT_WITH_FULL_KEY_ENCRYPTION_FLAG,AES_ENCRYPTION_FLAG, AES_CORPORATE_ENCRYPTION_FLAG, AES_DEFAULT_ENCRYPTION_FLAG, VAULT_AES_ENCRYPTION_FLAG,AES_DEFAULT_WITH_ENC_ENCRYPTION_FLAG,AES_DYNAMIC_WITH_ENC_ENCRYPTION_FLAG,AES_CORPORATE_WITH_ENC_ENCRYPTION_FLAG,AES_DEFAULT_WITH_FULL_KEY_ENC_ENCRYPTION_FLAG,AES_DEFAULT_WITH_FULL_KEY_ENCRYPTION_FLAG_NEW,AES_DEFAULT_WITH_FULL_KEY_ENC_ENCRYPTION_FLAG_NEW);
    public static final List<String> PLAIN_ENCRYPTION_LIST=Arrays.asList(HEX_ENC_ENCRYPTION_FLAG,HEX_ENCRYPTION_FLAG,PLAIN_ENCRYPTION_FLAG);
    public static final List<String> VAULT_ENCRYPTION_LIST=Arrays.asList(HEX_ENCRYPTION_FLAG,PLAIN_ENCRYPTION_FLAG,VAULT_AES_ENCRYPTION_FLAG);
    public static final List<String> ENC_ENCRYPTION_LIST=Arrays.asList(HEX_ENC_ENCRYPTION_FLAG,AES_DEFAULT_WITH_ENC_ENCRYPTION_FLAG, AES_DYNAMIC_WITH_ENC_ENCRYPTION_FLAG,AES_CORPORATE_WITH_ENC_ENCRYPTION_FLAG,AES_DEFAULT_WITH_FULL_KEY_ENC_ENCRYPTION_FLAG,AES_DEFAULT_WITH_FULL_KEY_ENC_ENCRYPTION_FLAG_NEW);

    //SquareTrade
    public static String SQUARETRADE_ACCESS_TOKEN = "";

    //Target
    public static final String NO_CARRIER = "0";
    public static final String SPRINT_ID = "1";
    public static final String SQUARETRADE_ID = "2";
    public static final String ATT_ID = "3";
    public static final String CONSUMER_CELLULER_ID = "4";
    public static final String SARAPLUS_ID = "5";
    public static final String TRACFONE = "6";

    //Insurance
    public static final String E_PAY    = "3";

    public static final String SPRINT_PROCESSOR_ID            = "551";
    public static final String SQUARE_TRADE_PROCESSOR_ID      = "552";
    public static final String ATT_PROCESSOR_ID               = "553";
    public static final String CONSUMER_CELLULER_PROCESSOR_ID = "554";
    public static final String SARAPLUS_PROCESSOR_ID          = "555";
    public static final String EPAY_PROCESSOR_ID              = "556";
    public static final String TRACFONE_PROCESSOR_ID          = "557";

    public static final String TOKEN_STATUS = "0";
    public static final String USE_SAKA_FLAG = "1";
    public static final String USE_AURUS_VAULT_FLAG = "1";

    // P2pe Flag
    public static final String P2PE_FLAG_OFF      = "00";
    public static final String P2PE_FAIL_CODE_OFF = "00";
    public static final String STPL_RSA_FLAG      = "3";
    public static final String P2PE_FLAG_ON                     = "01";
    public static final String P2PE_TRANSARMOR_RSA_FLAG_ON      = "02";
    public static final String P2PE_ONGUARD_FLAG_ON             = "03";
    public static final String P2PE_TRANSARMOR_TAVE_FLAG_ON     = "04";
    public static final String P2PE_PROCESSOR_ONGUARD_FLAG_ON   = "05";
    public static final String P2PE_DUKPT_ENCRYPTION_FLAG_ON    = "06";
    public static final String TOKENIZED_FLAG_ON                = "07";
    public static final String P2PE_ONGUARD_SDE_FLAG_ON         = "08";
    public static final String P2PE_BLUEFIN_DECRYPTX_FLAG       = "09";
    public static final String P2PE_APPLE_ENCRYPTION_FLAG_ON    = "10";
    public static final String P2PE_PROCESSOR_ONGUARD_SDE_FLAG_ON   = "11";
    public static final String P2PE_FLAG_ON_ENCODED                 = "12";


    public static final List<String> P2PE_FLAG_NOT_CI_GEN_LIST = Arrays.asList(P2PE_TRANSARMOR_RSA_FLAG_ON,P2PE_TRANSARMOR_TAVE_FLAG_ON,P2PE_PROCESSOR_ONGUARD_FLAG_ON,P2PE_DUKPT_ENCRYPTION_FLAG_ON);
    public static final List<String> P2PE_FLAG_ENC_CARD_LIST = Arrays.asList(P2PE_TRANSARMOR_RSA_FLAG_ON,P2PE_PROCESSOR_ONGUARD_FLAG_ON,P2PE_DUKPT_ENCRYPTION_FLAG_ON);

    // HSM Flag
    public static final String HSM_FLAG_ON = "01";
    public static final String HSM_FLAG_OFF = "00";

    public static final String DEBIT = "DEBIT";
    public static final String CREDIT = "CREDIT";
    public static final String GIFT = "GIFT";
    public static final String PLCC = "PLCC";
    public static final String CHECK = "CHECK";

    public static final String EBT = "EBT";
    public static final String FLEET = "FLEET";
    public static final String FSA = "FSA";
    public static final String PREPAID = "PREPAID";

    public static final String OTT_FLAG = "1";
    public static final String OOT_FLAG = "2";
    
    public static final String ERROR_MSG = "ERROR MSG : ";
    public static final String ISO_FIELD_DECIMAL_FORMAT = "##.##";
    public static final String LEFT_ANGLUAR = "%3C";
    public static final String RIGHT_ANGLUAR = "%3E";
    public static final String GS = "%1D";
    public static final String RS = "%1E";
    public static final String TILED = "%7E";
    public static final String SLASH = "%2F";
    public static final String BATCH_COUNTER = "001";
    public static final String APPROVAL_CODE = "000000";

    public static final List<String> TRANSACTION_TYPE_LIST = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "10", "11", "12", "13", "14", "21", "22", "23", "24", "25", "26", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "57", "58", "63", "64", "82", "100", "103", "104", "105", "106", "107", "108", "109", "110", "111", "113", "115", "201", "202", "203", "204", "205", "206", "207", "208", "209", "210", "211", "212", "112", "91", "116", "117", "9", "119", "29", "221", "222", "225", "226", "228", "229", "241", "242", "243", "244", "245", "246", "261", "262", "263", "264", "265", "266", "95", "281", "282", "102", "120", "118", "96", "121", "122", "92", "97", "123", "124", "56", "125", "126", "127", "128", "286", "285", "129", "130", "131","132","133","137","144","230","134","224","227","283","301", "302", "306", "307", "310", "59", "283", "287", "284","135","136","138","213","142","143","145","214","141","146","148","288","289","215","147","150","151","152","153","155","216","154","290","157","28","291","156","158","159","305","160","268","269","292","293","294","321","322","323","324","325","326","327","161","162","311","165","164","166","167","168","169","16", "30", "218", "98","232","233","170","171","173","247","174","223","90","451","452","453","454","455","456","457","458","459");
    public static final List<String> SINGLE_TRANS_TYPE_LIST = Arrays.asList("1", "3", "8", "10", "11", "12", "13", "21", "23", "27", "41", "42", "44", "45", "46", "47", "50", "52", "54", "55", "56", "57", "201", "203", "207", "208", "209", "210", "211", "212", "91", "9", "119", "29", "221", "229", "241", "243", "261", "263","269" ,"228", "281", "282", "122", "129","230","134","224","227","301","307","310","283","287","59","214","288","289","215","100","290","28","291" , "156","305","268","292","293","294","321","322","323","324","325","326","311","223","451","452","453","454","455","456","457","458","459");
    public static final List<String> SPLIT_TRANS_TYPE_LIST = Arrays.asList("2", "4", "5", "6", "7","14" ,"22", "24", "25", "26", "43", "53","48", "49", "51", "58", "202", "204", "205", "206", "222", "225", "226", "242", "244", "245", "246", "262", "264", "265", "266", "96", "92", "97", "286", "285","302","305","306","284","213","327","16", "30", "218", "98","232","233","95");

    public static final List<String> GIFT_TRANSACTION_LIST = Arrays.asList("41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58","59");
    public static final List<String> CREDIT_TRANSACTION_LIST = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "16", "119", "14", "131" , "156","123","321","451","452","453","454","455","456","457","458","459");
    public static final List<String> DEBIT_TRANSACTION_LIST = Arrays.asList("21", "22", "23", "24", "25", "27", "28", "29", "26", "63","30");
    public static final List<String> CASH_TRANSACTION_LIST = Arrays.asList("81", "82", "83","84");
    public static final List<String> PLCC_TRANSACTION_LIST = Arrays.asList("201", "205", "202", "203", "204", "211", "207", "208", "209", "210", "212", "206", "131","213","214","215","218");
    public static final List<String> CHECK_TRANSACTION_LIST = Arrays.asList("91", "92", "95", "97", "96", "100","98");
    public static final List<String> EBT_TRANSACTION_LIST = Arrays.asList("221", "222", "225", "228", "229", "226", "230", "224", "227", "142", "143","232","233","223");
    public static final List<String> FLEET_TRANSACTION_LIST = Arrays.asList("241", "242", "243", "244", "245", "246");
    public static final List<String> FSA_TRANSACTION_LIST = Arrays.asList("261", "262", "263", "264", "265", "266", "268","269");
    public static final List<String> PREPAID_TRANSACTION_LIST = Arrays.asList("281", "282", "286", "285","283","134","287","284","288","289","290","291","292","293","294");
    public static final List<String> EPP_TRANSACTION_LIST = Arrays.asList("301", "302", "305", "306", "307", "310","311");
    public static final List<String> LOYALTY_TRANSACTION_LIST = Arrays.asList("321","322","323","324","325","326","327");

    public static final List<String> ORIGINAL_TRANS_REQ_LIST = Arrays.asList("14","4","5","6", "7", "26", "51","55","49","205","206","244","246","286","306","204","58","292","327","264","265","266");
    public static final List<String> NO_CUSTOM_VALIDATION = Arrays.asList("63", "100", "103", "104", "105", "106", "108", "109", "110", "111", "113", "115", "64", "112","116","117","120","118","121","122","123","125","133","135","136","137","144","145","160","90");
    public static final List<String> BYPASS_VALIDATION_TRANS_TYPE = Arrays.asList("107","208","102","149","216");
    public static final List<String> ARRAYLISTCHECK = Arrays.asList("5", "6", "51", "26", "206", "91", "95", "226", "246", "266", "96", "286", "285", "63", "4", "109", "121", "138", "142", "143" ,"100","215","148","128","130","132","154","290","291" ,"156","123","292","451","452","453","454","455","456","457","458","459");
    public static final List<String> SALE_LIST = Arrays.asList("1", "21", "42","201","221","241","261","282","91","134","301","59","287","321");
    public static final List<String> PRESALE_DATE_ALLOW_TRANS_LIST = Arrays.asList("1", "2", "3", "4", "5", "7", "21", "23", "24", "201", "202", "205", "207", "6", "26", "206","129");
    public static final List<String> OFFLINE_TRANSACTION_LIST = Arrays.asList("2", "4", "7");
    public static final List<String> MULTY_TRANS_LIST = Arrays.asList("1","21","41","42","52","201","207","221","241","261","281","91","8","214","23","57","3","203","91","221");
    public static final List<String> UPDATE_TRANS = Arrays.asList("7","27","49","213");
    public static final List<String> REFUND_TRANS_LIST = Arrays.asList("2", "22", "43","202","222","242","262","92","97","302","284","53");
    public static final List<String> SINGLE_TRANS_LIST = Arrays.asList("2", "22", "26", "43", "51", "82","202","92");
    public static final List<String> VOID_REV_TRANS_LIST = Arrays.asList("5", "25", "48", "6", "26", "51","205","206","95","225","226","245","246","265","266","96","97","285","286","305","306","327");
    public static final List<String> TERMINALLIST = Arrays.asList("21535226");
    public static final List<String> CORPLIST = Arrays.asList("873");
    public static final List<String> DEBITASCREDIT = Arrays.asList("FDMS Processor");
    public static final List<String> OFFLINE_TIP_PROCESSOR = Arrays.asList(Constants.CREDIT_NOVA_PROCESSOR, Constants.CREDIT_PAYMENTTECH, Constants.CREDIT_FDMS_PROCESSOR, Constants.CREDIT_HEARTLAND_PROCESSOR, Constants.CREDIT_TSYS_PROCESSOR, Constants.CREDIT_WORLDPAY_PROCESSOR, Constants.CREDIT_FD_OMAHA, Constants.CREDIT_ELAVON_PROCESSOR,Constants.CREDIT_FD_RAPID_CONNECT_PROCESSOR,Constants.CREDIT_VANTIV_ISO_PROCESSOR,Constants.PLCC_CAPITAL_ONE,Constants.PLCC_CAPITAL_ONE_ISO,Constants.CREDIT_FD_TRANSARMOR_PROCESSOR,Constants.CREDIT_AMEX_PROCESSOR);
    public static final List<String> BYPASS_VALIDATION_ENTRY_DS = Arrays.asList(Constants.CASH_ENTRY_DATASOURCE, Constants.KI_ENTRY_DATA_SOURCE, Constants.ALREADY_PAY_ENTRY_DATA_SOURCE,Constants.CHECK_ENTRY_DATASOURCE);
    public static final List<String> BYPASS_AMT_ZERO_CHECK = Arrays.asList(Constants.FSA_AVS,Constants.GIFT_ISSUE_VIRTUAL_CARD,Constants.GIFT_ISSUE,Constants.GIFT_BALANCE_INQURY, Constants.AVS_CHECK, Constants.PLCC_ACCOUNT_LOOKUP, Constants.PLCC_CREDIT_APPLICATION, Constants.PLCC_BALANCE_INQUIRY, Constants.PLCC_INSTANT_CREDIT, Constants.PLCC_QUICK_CREDIT, Constants.CREDIT_BALANCEENQUIRY, Constants.DEBIT_BALANCEENQUIRY, Constants.EBT_BALANCE_INQUIRY, Constants.PROCESSOR_CARD_TOKEN, Constants.GIFT_CASHOUT, Constants.GIFT_DEACTIVAT_WITHREFUND, Constants.GIFT_DEACTIVAT_WITHOUTREFUND, Constants.GIFT_POST_AUTH, Constants.PLCC_PREAUTH, Constants.GIFT_BALANCE_TRANSFER, Constants.MERCHANDISE_RETURN, Constants.CREDIT_TIP, Constants.GIFT_TIP, Constants.EPP_ITEM_QUALIFICATION, Constants.EPP_BALANCE_LOOKUP, Constants.PLCC_TIP, Constants.PLCC_AVS, Constants.CREDIT_PREAUTH,Constants.SIGNATURE,Constants.PREPAID_BALANCE_INQUIRY,Constants.DEBIT_ZERO_DOLLAR,Constants.PREPAID_DISBURSEMENT,Constants.LOYALTY_GET_REWARD,Constants.PLCC_ORDER,Constants.CREDIT_VOID,Constants.DEBIT_VOID,Constants.GIFT_VOID,Constants.PLCC_VOID,Constants.EBT_VOID,Constants.FLEET_VOID,Constants.FSA_VOID,Constants.PREPAID_VOID,Constants.EPP_VOID,Constants.CREDIT_SALE, Constants.GIFT_PRE_AUTH);
    public static final List<String> BYPASS_TOTAL_AMOUNT_CHECK=Arrays.asList(Constants.GIFT_BALANCE_INQURY,Constants.PLCC_CREDIT_APPLICATION,Constants.AVS_CHECK,Constants.PLCC_INSTANT_CREDIT,Constants.PLCC_QUICK_CREDIT,Constants.PLCC_ACCOUNT_LOOKUP,Constants.THIRD_PARTY_TOKEN,Constants.EPP_BALANCE_LOOKUP , Constants.PLCC_AVS,Constants.PREPAID_BALANCE_INQUIRY,Constants.DEBIT_ZERO_DOLLAR);
    public static final List<String> P2PE_SUB_TRANS_TYPE = Arrays.asList("9","10");
    public static final List<String> RSA_FLAG_LIST = Arrays.asList("1","2");
    public static final List<String> REPROCESSING_TRANS_LIST = Arrays.asList("1","3","21","42","201","2","22","43","202");
    public static final List<String> TARGET_TRANSACTION_LIST = Arrays.asList("109", "110");
    public static final List<String> UPLOAD_TRANSACTION_LIST = Arrays.asList("103", "104", "105");
    public static final List<String> GET_PRODUCT_TRANS_LIST = Arrays.asList("127", "111", "115");
    public static final List<String> BYPASS_TXN_ABORT = Arrays.asList(Constants.PROCESS_TRANS, Constants.MERCHANT_AUTH_API);
    public static final List<String> VOID_TRANS_LIST = Arrays.asList("5", "25", "48", "205", "225", "95", "245", "265", "285", "305");

    public static final List<String> PREAUTH_LIST = Arrays.asList("3","23","57","203","243","263");
    public static final List<String> POSTAUTH_LIST = Arrays.asList("4","24","58","204","244","264","233");

    public static final List<String> RAPID_PROCESSOR_LIST = Arrays.asList(Constants.CREDIT_FD_RAPID_CONNECT_PROCESSOR,Constants.DEBIT_FD_RAPID_CONNECT_PROCESSOR,Constants.GIFT_FD_RAPID_CONNECT_PROCESSOR,Constants.EBT_FD_RAPID_CONNECT_PROCESSOR,Constants.CHECK_FD_RAPID_CONNECT_PROCESSOR,Constants.FSA_FD_RAPID_CONNECT_PROCESSOR,Constants.PREPAID_FD_RAPID_CONNECT_PROCESSOR,Constants.FLEET_FD_RAPID_CONNECT_PROCESSOR,Constants.PLCC_FD_RAPID_CONNECT_PROCESSOR,Constants.CREDIT_FD_RAPID_CONNECT_CANADA_PROCESSOR,Constants.DEBIT_FD_RAPID_CONNECT_CANADA_PROCESSOR);
    public static final List<String> TRANSARMOR_PROCESSOR_LIST = Arrays.asList(Constants.CREDIT_FD_TRANSARMOR_PROCESSOR,Constants.DEBIT_FD_TRANSARMOR_PROCESSOR,Constants.EBT_FD_TRANSARMOR_PROCESSOR,Constants.FLEET_FD_TRANSARMOR_PROCESSOR,Constants.CREDIT_FD_TRANSARMOR_CANADA_PROCESSOR,Constants.DEBIT_FD_TRANSARMOR_CANADA_PROCESSOR);
    public static final List<String> SELF_REGISTRATION_PROCESSOR_LIST = Arrays.asList(Constants.CREDIT_FD_RAPID_CONNECT_PROCESSOR,Constants.DEBIT_FD_RAPID_CONNECT_PROCESSOR,Constants.GIFT_FD_RAPID_CONNECT_PROCESSOR,Constants.EBT_FD_RAPID_CONNECT_PROCESSOR,Constants.CHECK_FD_RAPID_CONNECT_PROCESSOR,Constants.FSA_FD_RAPID_CONNECT_PROCESSOR,Constants.PREPAID_FD_RAPID_CONNECT_PROCESSOR,Constants.CREDIT_FD_TRANSARMOR_PROCESSOR,Constants.DEBIT_FD_TRANSARMOR_PROCESSOR,Constants.EBT_FD_TRANSARMOR_PROCESSOR,Constants.FLEET_FD_TRANSARMOR_PROCESSOR,Constants.FLEET_FD_RAPID_CONNECT_PROCESSOR,Constants.PLCC_FD_RAPID_CONNECT_PROCESSOR,Constants.CREDIT_FD_RAPID_CONNECT_CANADA_PROCESSOR,Constants.DEBIT_FD_RAPID_CONNECT_CANADA_PROCESSOR,Constants.CREDIT_FD_TRANSARMOR_CANADA_PROCESSOR,Constants.DEBIT_FD_TRANSARMOR_CANADA_PROCESSOR);

    public static final List<String> FD_HCS_CREDIT_PROCESSOR_LIST = Arrays.asList(Constants.CREDIT_FD_RAPID_CONNECT_PROCESSOR,Constants.CREDIT_FD_TRANSARMOR_PROCESSOR,Constants.CREDIT_FD_TRANSARMOR_CANADA_PROCESSOR,Constants.CREDIT_FD_RAPID_CONNECT_CANADA_PROCESSOR);
    public static final List<String> FD_HCS_DEBIT_PROCESSOR_LIST  = Arrays.asList(Constants.DEBIT_FD_RAPID_CONNECT_PROCESSOR,Constants.DEBIT_FD_TRANSARMOR_PROCESSOR,Constants.DEBIT_FD_TRANSARMOR_CANADA_PROCESSOR,Constants.DEBIT_FD_RAPID_CONNECT_CANADA_PROCESSOR);

    public static final List<String> NON_FINANCIAL_TRANS_LIST = Arrays.asList("41", "52", "44", "46", "47", "281", "283", "288","158","159","161","162","50","307","310","289");

    public static final List<String> APPLEPAY_ENTRY_DATA_LIST = Arrays.asList("9", "10", "11");

    public static final List<String> REVERSAL_TRANS_LIST = Arrays.asList("6", "26","96","51","206","226","246","266","286","306","327");
    public static final List<String> SNF_SUPPROTED_TRANS_LIST = Arrays.asList("1", "21");
    public static final List<String> OFFLINE_TRANS_LIST = Arrays.asList("1","2","5","201","202","205","241","242","245","281","261","262","265","42","43","48","8","3","4");
    public static final List<String> RNF_SUPPORTED_TRANS_LIST = Arrays.asList("1","3","4","21","23","24","8","201","203","214","221","223");
    public static final List<String> RNF_TOKEN_BLOCKING_TRANS_LIST = Arrays.asList(Constants.TRANSACTION_TOKEN, Constants.CREDIT_VOID, Constants.CREDIT_REFUND, Constants.DEBIT_REFUND, Constants.PLCC_PREAUTH, Constants.PLCC_POSTAUTH, Constants.PLCC_VOID, Constants.PLCC_RETURN, Constants.PLCC_AVS);

    // Risk and Fraud
    public static final List<String> RNF_CVV_MISMATCH_LIST = Arrays.asList("NoMtch", "NotPrc", "Unknwn", "N", "P");
    public static final List<String> RNF_ZIP_MISMATCH_LIST = Arrays.asList("A", "B","I");
    public static final List<String> RNF_ADDR_MISMATCH_LIST = Arrays.asList("P", "U", "W", "Z", "N","I");
    public static final List<String> RNF_ZIP_ADDR_MISMATCH_LIST = Arrays.asList("C", "N", "E","I");
    public static final List<String> POST_TIP_TRANS_LIST = Arrays.asList("7","27","49","213");
    public static final List<String> VANTIV_ZIP_ADDR_MATCH_LIST = Arrays.asList("B","C","E","R","S","U","X","Y","D","F","G","I","M");
    public static final List<String> ADS_ECOMM_ZIP_ADDR_MATCH_LIST = Arrays.asList("X","Y");//Added For ADS E-COMM
    public static final List<String> FD_TB_CVV_MATCH_LIST = Arrays.asList("Match","NotPrc","NotPrv","NotPrt","Unknwn");
    public static final List<String> FD_TB_AVS_ZIP_ADDR_MATCH_LIST = Arrays.asList("B","C","D","E","F","G","I","K","L","M","O","P","R","S","U","W","X","Y","Z" );
    public static final List<String> CHASE_ECOMM_ZIP_ADDR_MATCH_LIST = Arrays.asList("D","F","M","P","T","W","X","Y","Z");//Added For CHASE E-COMM
    public static final List<String> CHASE_CVV_MISMATCH_LIST = Arrays.asList("F","M","T");//Added For CHASE E-COMM
    public static final List<String> ORBITAL_AVS_ZIP_ADDR_MISMATCH_LIST = Arrays.asList("9","H","JA","M1","M2","M3","M4","M5","M6","M7","N5","I3","IA","N7","N8","N9","R","X","Z","B","A","C","7");
    public static final List<String> MEIJERS_ZIP_ADDR_MISMATCH_LIST = Arrays.asList("B","C","D","E","G","I","M","P","S","U","W","X","Y","Z");
    public static final List<String> VANTIV_ISO_ZIP_ADDR_MATCH_LIST = Arrays.asList("A","B","C","E","N","P","Z","R","S","U","W","X","Y","D","F","G","I","M");

    public static final List<String> MINOR_UNIT_ZERO = Arrays.asList("952","108","950","174","262","953","324","392","410","600","646","800","548","704","152");
    public static final List<String> MINOR_UNIT_THREE = Arrays.asList("048","648","368","400","414","434","512","788" );

    public static final List<String> POST_AUTH_LIST = Arrays.asList("4", "204", "58","24","244", "264");

    public static final List<String> CREDIT_CARD_TYPE_LIST = Arrays.asList("AX", "DC", "DS", "JB", "MS", "VI", "MC", "NV");
    public static final List<String> CR_CARD_TYPE_LIST = Arrays.asList( "1", "2", "3", "4", "5", "6", "7", "8");
    public static final List<String> REWARDS_INDICATOR_LIST = Arrays.asList("1", "2", "3");
    public static final List<String> PINLESS_DEBIT_OPTIMIZATION_CLIENT = Arrays.asList(Constants.DOLLAR_GENERAL_VERSION, Constants.TRADER_JOE_VERSION,Constants.SCHNUCKS_VERSION,Constants.WEGMANS_VERSION);
    public static final String STATUS_INPROGRESS = "50";

    public static final List<String> VANTIV_ISO_HCS_API = Arrays.asList(Constants.KROGER_VERSION);

    public static final String HSM  = "11";
    public static final String IDTECH = "12";
    public static final String LEAF = "13";
    public static final String ROAM = "14";
    public static final String ROAMEMV = "15";
    public static final String SWIPE_ENTRY_MODE         = "S";
    public static final String MANUAL_ENTRY_MODE        = "K";
    public static final String CHIP_ENTRY_MODE          = "I";
    public static final String TAB_ENTRY_MODE           = "T";
    public static final String CARD_OF_FILE_ENTRY_MODE  = "C";
    public static final String BAR_CODE_ENTRY_MODE      = "B";
    public static final String CI_ENTRY_MODE            = "A";
    public static final String PROCESSOR_TOKEN_ENTRY_MODE  = "P";

    // Entry Data Source Type
    public static final String T1_ENTRY_DATASOURCE              = "1";
    public static final String T2_ENTRY_DATASOURCE              = "2";
    public static final String KEYED_ENTRY_DATASOURCE           = "3";
    public static final String T1_T2_ENTRY_DATASOURCE           = "4";
    public static final String CASH_ENTRY_DATASOURCE            = "5";
    public static final String EMV_ENTRY_DATASOURCE             = "6";
    public static final String PAYPAL_T1_ENTRY_DATASOURCE       = "9";
    public static final String PAYPAL_T2_ENTRY_DATASOURCE       = "10";
    public static final String PAYPAL_T1_T2_ENTRY_DATASOURCE    = "11";
    public static final String KI_ENTRY_DATA_SOURCE             = "12";
    public static final String ALREADY_PAY_ENTRY_DATA_SOURCE    = "13";
    public static final String CHECK_ENTRY_DATASOURCE           = "14";
    public static final String TOKEN_ENTRY_DATASOURCE           = "15";
    public static final String PROCESSOR_TOKEN_ENTRY_DATASOURCE = "16";
    public static final String EMV_CONTACTLESS_ENTRY_DATASOURCE = "17";
    public static final String BAR_CODE_ENTRY_DATASOURCE        = "18";
    public static final String THREE_D_SECURE_ENTRY_DATASOURCE  = "19";
    public static final String QR_CODE_ENTRY_DATASOURCE         = "20";
    public static final String MERCHANT_TOKEN_ENTRY_DATASOURCE  = "21";
    public static final String CRM_TOKEN_ENTRY_DATASOURCE       = "22";    //Only STPL using for reporting purpose
    public static final String VPA_ENTRY_DATASOURCE             = "23";
    public static final String NETWORK_TOKEN_ENTRY_DATASOURCE   = "24";

    // Transaction Type
    public static final String CREDIT_SALE = "1";
    public static final String CREDIT_REFUND = "2";
    public static final String CREDIT_PREAUTH = "3";
    public static final String CREDIT_POSTAUTH = "4";
    public static final String CREDIT_VOID = "5";
    public static final String CREDIT_REVERSAL = "6";
    public static final String CREDIT_TIP = "7";
    public static final String CREDIT_ZERO_DOLLAR = "8";
    public static final String AVS_CHECK = "8";
    public static final String CREDIT_BALANCEENQUIRY = "9";
    public static final String CREDIT_CREATE_PROFILE  = "10";
    public static final String CREDIT_UPDATE_PROFILE = "11";
    public static final String CREDIT_RETRIEVE_PROFILE = "12";
    public static final String CREDIT_DELETE_PROFILE = "13";
    public static final String CREDIT_INCREMENTAL_AUTH = "14";
    public static final String CREDIT_RE_AUTH          = "15";
    public static final String CREDIT_PRE_REFUND       = "16";

    public static final String DEBIT_SALE = "21";
    public static final String DEBIT_REFUND = "22";
    public static final String DEBIT_PREAUTH = "23";
    public static final String DEBIT_POSTAUTH = "24";
    public static final String DEBIT_VOID = "25";
    public static final String DEBIT_REVERSAL = "26";
    public static final String DEBIT_TIP = "27";
    public static final String DEBIT_ZERO_DOLLAR = "28";
    public static final String DEBIT_BALANCEENQUIRY = "29";
    public static final String DEBIT_PRE_REFUND     = "30";

    public static final List<String> FINANCIAL_TRANS_LIST = Arrays.asList("1","2","3","4","5","6","7","14","15","21","22","23","24","25","26","27");

    public static final String GIFT_ACTIVATION = "41";
    public static final String GIFT_REDEEM = "42";
    public static final String GIFT_RETURN = "43";
    public static final String GIFT_BALANCE_INQURY = "44";
    public static final String GIFT_BALANCE_TRANSFER = "45";
    public static final String GIFT_DEACTIVAT_WITHREFUND = "46";
    public static final String GIFT_DEACTIVAT_WITHOUTREFUND = "47";
    public static final String GIFT_VOID = "48";
    public static final String GIFT_TIP = "49";
    public static final String GIFT_RELOAD = "50";
    public static final String GIFT_REVERSAL = "51";
    public static final String GIFT_ISSUE = "52";
    public static final String MERCHANDISE_RETURN = "53";
    public static final String GIFT_CASHOUT = "54";
    public static final String GIFT_ISSUE_VIRTUAL_CARD = "55";
    public static final String GIFT_BALANCE_MERGE = "56";
    public static final String GIFT_PRE_AUTH = "57";
    public static final String GIFT_POST_AUTH = "58";
    public static final String GIFT_SALE_ACTIVE_CODE = "59";

    public static final String KEY_EXCHANGE_CODE        = "63";
    public static final String CORP_KEY_EXCHANGE_CODE   = "64";
    public static final String CASH_ORDER               = "81";
    public static final String CASH_REFAUND             = "82";
    public static final String CASHVOID                 = "83";
    public static final String CASH_REVERSAL            = "84";
    public static final String TRANSACTION_SEARCH_API   = "90";
    public static final String SIGNATURE                = "100";
    public static final String CARD_TOKEN               = "101";
    public static final String TRANSACTION_TOKEN        = "102";
    public static final String RECEIPT_UPLOAD           = "103";
    public static final String SURVEY_UPLOAD            = "104";
    public static final String PRODUCT_UPLOAD           = "105";
    public static final String CUSTOMER_REGISTRATION_CODE ="106";
    public static final String GET_CARD_BIN_CODE        ="107";
    public static final String PROCESS_TRANS            = "108";
    public static final String COMMIT_TRANS             = "109";
    public static final String CHECK_STATUS_TRANS       = "110";
    public static final String GET_PRODUCT_DETAILS      = "111";
    public static final String GET_CAP_KEYS             ="112";
    public static final String GET_CARD_DATA_CODE       ="113";
    public static final String CANCEL                   = "115";
    public static final String TRANSACTION_STATUS       = "116";
    public static final String SP_ACCOUNT_KI_API_CODE   ="117";
    public static final String SP_UPDATE_CARD_INFO_API_CODE ="118";
    public static final String PROCESSOR_CARD_TOKEN     = "119";
    public static final String CLOSE_TRANSACTION        ="120";
    public static final String SETTLEMENT_CODE          ="121";
    public static final String ECOM_TOKEN_TRANS_CODE    = "122";
    public static final String UPDATE_AURUS_IVR_RECORD  = "123";
    public static final String TRANSACTION_HISTORY      = "124";
    public static final String TRANS_HISTORY_CODE       = "124";
    public static final String GENERATE_SESSION_ID      = "125";
    public static final String UPDATE_CARD_EXPIRY_CODE  = "126";
    public static final String GET_SUBSCRIPTION_DETAILS = "127";
    public static final String SESSION_STATUS_TRANS_CODE = "128";
    public static final String DCC_RATE                = "129";
    public static final String BILLING_TOKEN           = "130";
    public static final String THIRD_PARTY_TOKEN       ="131";
    public static final String GET_SESSION_TOKEN       = "132";
    public static final String MERCHANT_AUTH_API       = "133";
    public static final String UPDATE_BILLING_SHIPPING_DETAILS = "134";
    public static final String ORDER_SUBMIT            = "135";
    public static final String ORDER_CONFIRM           = "136";
    public static final String ORDER_CANCEL            = "137";
    public static final String ORDER_PROCESS           = "138";
    public static final String ORDER_RETURN            = "139";
    public static final String ORDER_UPDATE            = "140";
    public static final String REQUEST_TOKEN           = "141";
    public static final String BIN_CHECK               = "142";
    public static final String INITIATE                = "143";
    public static final String ACTIVE_TERMINAL         = "144";
    public static final String REGISTER_AMOUNT_UPDATE  = "145";
    public static final String IPN                     = "146";
    public static final String CUSTOMER_DATA_REMOVAL   = "147";
    public static final String GET_CONFIGURATION       = "148";
    public static final String UPDATE_TOKEN            = "149";
    public static final String PAYMENT_PAGE_RENDERATION     = "150";
    public static final String CRM_OPERATIONS          = "151";
    public static final String RISK_INQUIRY            = "152";
    public static final String RISK_ACTION_STATUS      = "153";
    public static final String EXPRESS_PAY_ORDER       = "154";
    public static final String DECISION_UPDATE         = "155";
    public static final String START_PAYMENT           = "156";
    public static final String TRANSACTION_LOOKUP      = "157";
    public static final String REQUEST_NOUNCE          = "158";
    public static final String API_AUTHENTICATION      = "159";
    public static final String SUBMIT_TRANS_STATUS     = "160";
    public static final String ENABLEMENT_TOKEN        = "161";
    public static final String STATE_VALIDATION        = "162";
    public static final String CERT_DETAILS            = "163";
    public static final String CHARGE_BACK             = "164";
    public static final String IGLAS_EX_SERVICE        = "165";
    public static final String ORIS_EX_SERVICE         = "166";
    public static final String ST_OPEN_EX_SERVICE      = "167";
    public static final String ST_CLOSE_EX_SERVICE     = "168";
    public static final String ST_CUSTOMER_LOOKUP      = "169";
    public static final String MTLS_CERT_DOWNLOAD      = "170";
    public static final String CLIENT_CREDENTIALS_LOOKUP   = "171";
    public static final String VOUCHER_ORDER               = "172";
    public static final String MTLS_URL_LOOKUP             = "173";
    public static final String SMS_SERVICE                 = "174";
    public static final String CUSTOMER_VERIFICATION       = "175"; 
    public static final String SEARCH_REFERENCES           = "176";
    public static final String PAY_REFERENCE               = "177";
    public static final String CONFIRM_PAYMENT             = "178";
    public static final String CHECK_PAYMENTS              = "179";
    public static final String SEARCH_PAYMENTS              = "180";

    public static final String PLCC_ORDER               = "201";
    public static final String PLCC_RETURN              = "202";
    public static final String PLCC_PREAUTH             = "203";
    public static final String PLCC_POSTAUTH            = "204";
    public static final String PLCC_VOID                = "205";
    public static final String PLCC_REVERSAL            = "206";
    public static final String PLCC_PAYMENT_PROTOCOL    = "207";
    public static final String PLCC_ACCOUNT_LOOKUP      = "208";
    public static final String PLCC_INSTANT_CREDIT      = "210";
    public static final String PLCC_QUICK_CREDIT        = "209";
    public static final String PLCC_CREDIT_APPLICATION  = "211";
    public static final String PLCC_BALANCE_INQUIRY     = "212";
    public static final String PLCC_TIP                 = "213";
    public static final String PLCC_AVS                 = "214";
    public static final String PLCC_PROCESSOR_CARD_TOKEN = "215";
    public static final String PLCC_SUBMIT_CARD_APPLICATION = "216";
    public static final String PLCC_RE_AUTH                 = "217";
    public static final String PLCC_PRE_REFUND              = "218";


    // PAYPAGE/ IFRAME
    public static final int SESSION_ID_LENGTH           = 32;

    public static final String STPL_RQ_CORP_ID          ="s01";
    public static final String STPL_RQ_MERCHANT_ID      ="s02";
    public static final String STPL_RQ_STORE_ID         ="s03";
    public static final String STPL_RQ_TERMINAL_ID      ="s04";
    public static final String STPL_RQ_DOMAIN_ID        ="s05";
    public static final String STPL_RQ_LANG_IND         ="s06";
    public static final String STPL_RQ_API_VERSION      ="s07";
    public static final String STPL_RQ_SESSION_ID       ="s08";
    public static final String STPL_RQ_SESSION_VALIDITY ="s09";
    public static final String STPL_RQ_URL_TYPE         ="s10";
    public static final String STPL_RQ_TEMPLATE_ID      ="s11";
    public static final String STPL_RQ_PM_HOST_URL      ="s12";
    public static final String STPL_RQ_SC_HOST_URL      ="s13";
    public static final String STPL_RQ_CARD_TYPE        ="s14";
    public static final String STPL_RQ_MASKED_CARD_NO   ="s15";
    public static final String STPL_RQ_CI               ="s16";
    public static final String STPL_RQ_TRANS_TYPE       ="s17";
    public static final String STPL_RQ_REG_TYPE         ="s18";
    public static final String STPL_RQ_SOURCE_TRANSACTION_ID ="s19";
    public static final String STPL_RQ_PROCESSOR_TOKEN  ="s20";
    public static final String STPL_RQ_MERCHANT_TOKEN   ="s21";
    public static final String STPL_RQ_EXPIRY_DATE      ="s22";
    public static final String STPL_RQ_URL_INDICATOR    ="s23";
    public static final String STPL_RQ_SUB_CARD_TYPE    ="s25";
    public static final String STPL_RQ_CARD_TYPE_SUPPORT= "s26";
    public static final String STPL_RQ_CARD_TYPE_DISCLAIMER = "s27";
    public static final String STPL_RQ_CARD_BIN_RANGE   ="s28";
    public static final String STPL_ECOM_RES_URL        ="s06";
    public static final String STPL_ECOM_RES_CODE       ="s07";
    public static final String STPL_ECOM_RES_TEXT       ="s08";
    public static final String STPL_RQ_CVV_INDICATOR    ="s29";
    public static final String STPL_RQ_WALLET_IDENTIFIER ="s30";
    public static final String STPL_RQ_CURRENCY_CODE    ="s31";
    public static final String STPL_RQ_CUST_NAME        ="s32";
    public static final String STPL_RQ_AUTHENTICATION_CHECK_IND ="s33";
    public static final String STPL_BILLING_ZIP_CODE    ="s34";
    public static final String STPL_ECST_URL_TYPE       ="s35";
    public static final String STPL_ECST_EBT_TRANS_ID   ="s36";
    public static final String STPL_ECST_OTT            ="s37";
    public static final String STPL_ECST_AURUS_TRANS_ID ="s38";
    public static final String STPL_ECST_AURUS_RES_CODE ="s39";
    public static final String STPL_ECST_AURUS_RES_TEXT ="s40";
    public static final String STPL_IS_HMAC_SUPPORT     ="s41";
    public static final String STPL_FORM_FACTOR_ID      ="s42";
    public static final String STPL_RQ_TRANSACTION_AMOUNT ="s43";
    public static final String STPL_RQ_DCC_FLAG           ="s44";
    public static final String STPL_QR_STRING           ="s45";
    public static final String STPL_SUB_API_VERSION      ="s46";

    // CHECK TRANSACTION TYPE
    public static final String CHECK_AUTHORIZATION  = "91";
    public static final String CHECK_REFUND         = "92";
    public static final String CHECK_VOID           = "95";
    public static final String CHECK_REVERSAL       = "96";
    public static final String CHECK_ADJUSTMENT     = "97";
    public static final String CHECK_PRE_REFUND     = "98";


    //EBT TRANSACTION TYPE
    public static final String EBT_ORDER            = "221";
    public static final String EBT_RETURN           = "222";
    public static final String EBT_PREAUTH          = "223";
    public static final String EBT_BALANCE_LOOKUP   = "224";
    public static final String EBT_VOID             = "225";
    public static final String EBT_REVERSAL         = "226";
    public static final String EBT_ITEM_QUALIFICATION = "227";
    public static final String EBT_VOUCHER_CLEAR    = "228";
    public static final String EBT_BALANCE_INQUIRY  = "229";
    public static final String EBT_CASH_WITHDRAWAL  = "230";
    public static final String EBT_ADJUSTMENT       = "231";
    public static final String EBT_PRE_REFUND       = "232";
    public static final String EBT_POSTAUTH         = "233";

    //FLEET TRANSACTION TYPE
    public static final String FLEET_ORDER          = "241";
    public static final String FLEET_RETURN         = "242";
    public static final String FLEET_PREAUTH        = "243";
    public static final String FLEET_POSTAUTH       = "244";
    public static final String FLEET_VOID           = "245";
    public static final String FLEET_REVERSAL       = "246";
    public static final String CGI_FLEET_RECON      = "247";

    //FSA TRANSACTION TYPE
    public static final String FSA_ORDER            = "261";
    public static final String FSA_RETURN           = "262";
    public static final String FSA_PREAUTH          = "263";
    public static final String FSA_POSTAUTH         = "264";
    public static final String FSA_VOID             = "265";
    public static final String FSA_REVERSAL         = "266";
    public static final String FSA_BALANCE_INQUIRY  = "267";
    public static final String FSA_AVS              = "268";
    public static final String FSA_INCREMENTAL_AUTH = "269";


    public static final long SESSION_ID_VALIDITY_IN_MILLIS = 1800000;
    public static final String SESSION_VALIDITY_FLAG = "1";

    //PREPAID TRANSACTION TYPE
    public static final String PREPAID_ACTIVATION       = "281";
    public static final String PREPAID_SALE             = "282";
    public static final String PREPAID_DEACTIVATION     = "283";
    public static final String PREPAID_REFUND           = "284";
    public static final String PREPAID_VOID             = "285";
    public static final String PREPAID_REVERSAL         = "286";
    public static final String PREPAID_SALE_ACTIVE_CODE = "287";
    public static final String PREPAID_BALANCE_INQUIRY  = "288";
    public static final String PREPAID_RELOAD           = "289";
    public static final String PREPAID_POR_REFUND       = "290";
    public static final String PREPAID_CASHOUT          = "291";
    public static final String PREPAID_DISBURSEMENT     = "292";
    //public static final String PREPAID_PREAUTH          = "293";
    //public static final String PREPAID_POSTAUTH         = "294";

    //EPP TRANSACTION TYPE
    public static final String EPP_SALE                 = "301";
    public static final String EPP_REFUND               = "302";
    public static final String EPP_VOID                 = "305";
    public static final String EPP_REVERSAL             = "306";
    public static final String EPP_BALANCE_LOOKUP       = "307";
    public static final String EPP_ITEM_QUALIFICATION   = "310";
    public static final String EPP_REDEEM               = "311";


    //Loyalty Transaction Type
    public static final String LOYALTY_GET_REWARD                    = "321";
    public static final String LOYALTY_CANCLE_REDEMPTION             = "322";
    public static final String LOYALTY_FINALIZE_REWARD               = "323";
    public static final String LOYALTY_CANCLE_TRANSACTION            = "324";
    public static final String LOYALTY_GET_STATUS_REWARD             = "325";
    public static final String LOYALTY_END_PERIOD_REQUEST            = "326";
    public static final String LOYALTY_REVERSAL_REQUEST              = "327";

    //UPI
    public static final String UPI_SALE                              = "401";

    //Wallet
    public static final String CREATE_WALLET                         = "451";
    public static final String GET_WALLET                            = "452";
    public static final String LIST_PAYMENTS                         = "453";
    public static final String GET_PAYMENT                           = "454";
    public static final String VALIDATE_ADDRESS                      = "455";
    public static final String CREATE_CHANNEL                        = "456";
    public static final String GET_PAYOUT                            = "457";
    public static final String VERIFY_BENEFICIARY_NAME               = "458";
    public static final String CONFIRM_BENEFICIARY_NAME              = "459";


    public static final String STX = "STX";
    public static final String ETX = "ETX";
    public static final String NA = "NA";
    public static final String ERROR_EXIST = "ERROR";
    public static final String FIELD_SEPARATOR = "\\[FS\\]";
    public static final String GROUP_SEPARATOR = "\\[GS\\]";
    public static final String KSN_MAC_SEPARATOR = "[GS]";

    //UNITED RENTAL
    public static final String REG_TYPE = "REG";

    // Credit Processor ID's(1-150)
    public static final String CREDIT_AUTHORIZENET = "1";
    public static final String CREDIT_PAYMENTTECH = "2";
    public static final String CREDIT_ORBITAL = "3";
    public static final String CREDIT_GLOBAL_PAYMENTS = "4";
    public static final String CREDIT_NOVA_PROCESSOR = "5";
    public static final String CREDIT_ICICI_PROCESSOR = "6";
    public static final String CREDIT_FDMS_PROCESSOR = "7";
    public static final String CREDIT_FD_NASHVILLE = "8";
    public static final String CREDIT_FD_OMAHA = "9";
    public static final String CREDIT_TRUCASH_PROCESSOR = "10";
    public static final String CREDIT_MERCURY = "11";
    public static final String CREDIT_TSYS_PROCESSOR = "12";
    public static final String CREDIT_MERCHANT_WAREHOUSE = "13";
    public static final String CREDIT_FD_BUYPASS_ISO = "14";
    public static final String CREDIT_NOVA_UK_PROCESSOR = "15";
    public static final String CREDIT_HEARTLAND_PROCESSOR = "16";
    public static final String CREDIT_GLOBAL_EMV_PROCESSOR = "17";
    public static final String CREDIT_GLOBAL_EASTHOST = "18";
    public static final String CREDIT_FDMS_BUYPASS_ATL = "19";
    public static final String CREDIT_BANCOMER_PROCESSOR = "20";
    public static final String CREDIT_WORLDPAY_PROCESSOR = "21";
    public static final String CREDIT_PAYPAL = "22";
    public static final String CREDIT_VANTIV_PROCESSOR = "23";
    public static final String CREDIT_BRAINTREE_PROCESSOR = "24";
    public static final String CREDIT_STRIPE_PROCESSOR = "25";
    public static final String CREDIT_E_PROCESSING = "26";
    public static final String CREDIT_DESJARDINS_PROCESSOR = "27";
    public static final String CREDIT_AMEX_PROCESSOR = "28";
    public static final String CREDIT_ELAVON_PROCESSOR = "29";
    public static final String CREDIT_FD_RAPID_CONNECT_PROCESSOR = "30";
    public static final String CREDIT_JETPAY_PROCESSOR = "31";
    public static final String CREDIT_VANTIV_ISO_PROCESSOR = "32";
    public static final String CREDIT_FD_TRANSARMOR_PROCESSOR = "33";
    public static final String CREDIT_CREDIMATIC_PROCESSOR = "36";
    public static final String CREDIT_CREDIBANCO_PROCESSOR = "37";
    public static final String CREDIT_BLUESNAP_PROCESSOR = "38";
    public static final String CREDIT_INTERDIN_PROCESSOR = "39";
    public static final String CREDIT_DATAFAST_PROCESSOR = "40";
    public static final String CREDIT_REDEBAN_PROCESSOR = "41";
    public static final String CREDIT_MONERIS_PROCESSOR = "42" ;
    public static final String CREDIT_ELAVON_CONVERGE_PROCESSOR = "44" ;
    public static final String CREDIT_FD_TRANSARMOR_CANADA_PROCESSOR = "45";
    public static final String CREDIT_VISANET_CREDIT = "46";
    public static final String CREDIT_CHASE_ISO_PROCESSOR = "47";
    public static final String CREDIT_FD_RAPID_CONNECT_CANADA_PROCESSOR = "48";
    public static final String CREDIT_PLANET_PROCESSOR = "49";
    public static final String CREDIT_TRANSBANK_PROCESSOR = "50";
    public static final String CREDIT_MC_PROCESSOR = "51";
    public static final String CREDIT_BORGUN_PROCESSOR = "52";
    public static final String CREDIT_CARDNET_UK_PROCESSOR = "53";
    public static final String CREDIT_BILLPOCKET_PROCESSOR = "54";
    public static final String CREDIT_STONE_PROCESSOR = "55";
    public static final String CREDIT_CIELO_PROCESSOR = "56";
    public static final String CREDIT_GLOBAL_PAYMENTS_UK_PROCESSOR = "57";
    public static final String CREDIT_BARCLAY_PROCESSOR = "58";
    public static final String CREDIT_COMERCIA_PROCESSOR = "59";
    public static final String CREDIT_SIXX_PAYMENTS_PROCESSOR = "60";
    public static final String CREDIT_CHASE_STRATUS            = "61";
    public static final String CREDIT_STARGATE_PROCESSOR      = "62";
    public static final String CREDIT_NPP_PROCESSOR         = "63";
    public static final String CREDIT_WORLDPAY_RAFT_PROCESSOR = "64";
    public static final String CREDIT_ELAVONISO_PROCESSOR      = "65";
    public static final String CREDIT_SWEDBANK_PROCESSOR = "67";
    public static final String CREDIT_FISERV_EUROPE_PROCESSOR = "69";
    public static final String CREDIT_GETNET_PROCESSOR = "70";

    // Alternate Payment Processor ID's(151-200)
    public static final String ALIPAY_PROCESSOR             = "151";
    public static final String PAYPAL_PROCESSOR             = "152";
    public static final String VISA_CHECKOUT_PROCESSOR      = "154";
    public static final String MASTERPASS_PROCESSOR         = "155";
    public static final String AMEX_PROCESSOR               = "156";
    public static final String SHOP_RUNNER_PROCESSOR        = "157";
    public static final String APPLEPAY_PROCESSOR           = "158";
    public static final String CHASEPAY_PROCESSOR           = "159";
    public static final String WECHAT_PROCESSOR             = "161";
    public static final String AMAZONPAY_PROCESSOR          = "162";
    public static final String GOOGLEPAY_PROCESSOR          = "163";
    public static final String AFTERPAY_PROCESSOR           = "164";
    public static final String KLARNAPAY_PROCESSOR          = "165";
    public static final String VENMO_BRAINTREE_PROCESSOR    = "166";
    public static final String SEZZLE_PROCESSOR             = "167";
    public static final String FLEXA_PROCESSOR              = "168";
    public static final String UNIONPAY_PROCESSOR           = "169";
    public static final String CITCON_PROCESSOR             = "170";
    public static final String FORUMPAY_PROCESSOR           = "171";
    public static final String TRUSTLY_PROCESSOR            = "172";
    public static final String AFFIRM_PROCESSOR             = "173";
    public static final String OTTPAY_PROCESSOR             = "174";
    public static final String SAFETYPAY_PROCESSOR          = "175";
    public static final String OXXOPAY_PROCESSOR            = "176";
    public static final String MERCADOPAGO_PROCESSOR        = "177";
    public static final String SYNCHRONY_PROCESSOR          = "178";
    public static final String GLN_PROCESSOR                = "179";
    public static final String PAZE_PROCESSOR               = "180";
    public static final String SAMSUNGPAY_PROCESSOR         = "181";
    public static final String KOTAK_UPI_PROCESSOR          = "182";
    public static final String SHOP_PAY_PROCESSOR           = "183";
    public static final String HYDEPAY_PROCESSOR            = "184";
    public static final String ALIPAYPLUS_PROCESSOR         = "185";
    public static final String UPI_PAY_PROCESSOR            = "186";
    public static final String PAYCO_PROCESSOR              = "187";
    public static final String LINE_PAY_PROCESSOR           = "188";
    public static final String RAKUTEN_PAY_PROCESSOR        = "189";
    public static final String NETS_PROCESSOR               = "190";
    public static final String GRABPAY_PROCESSOR            = "191";
    public static final String SHOPEEPAY_PROCESSOR          = "192";
    public static final String ATOME_PROCESSOR              = "193";
    public static final String TOSS_PROCESSOR               = "194";
    public static final String LG_PAY_PROCESSOR             = "195";
    public static final String SPEI_PROCESSOR               = "196";
    public static final String OXXO_PROCESSOR               = "197";
    public static final String UBP_PROCESSOR                = "198";
    public static final String PAYMAYA_PROCESSOR            = "199";
    public static final String CASH_APP_PROCESSOR           = "1101";
    public static final String OZOW_PROCESSOR               = "1102";
    public static final String M_PESA__PROCESSOR            = "1103";
    public static final String TRUEMONEY_PROCESSOR          = "1104";
    public static final String ALIPAY_HONGKONG_PROCESSOR    = "1105";
    public static final String TNG_PROCESSOR                = "1106";
    public static final String GCASH_PROCESSOR              = "1107";
    public static final String KAKAO_PAY_PROCESSOR          = "1108";
    public static final String NAVER_PAY_PROCESSOR          = "1109";
    public static final String TOSSPAY_PROCESSOR            = "1110";
    public static final String BOOST_PROCESSOR              = "1111";
    public static final String BPI_PROCESSOR                = "1112";
    public static final String DANA_PROCESSOR               = "1113";
    public static final String PAYNOW_PROCESSOR             = "1114";
    public static final String PAYPAY_PROCESSOR             = "1115";
    public static final String BVNK_STABLECOINS_PROCESSOR   = "1116";
    public static final String KPN_PROCESSOR                = "1117";
    public static final String L_PAY_PROCESSOR              = "1118";
    public static final String CITCON_UPI_PROCESSOR         = "1119";
   // public static final String HYDEPAY_PROCESSOR            = "1120";
    public static final String SPIRE_ACH_PROCESSOR          = "1121";
    public static final String ZIP_PAY_PROCESSOR            = "1123";
    public static final String TRUEMED_PROCESSOR            = "1124";

    // Debit Processor ID's (201-350)
    public static final String DEBIT_FDMS_PROCESSOR         = "201";
    public static final String DEBIT_FD_OMAHA               = "202";
    public static final String DEBIT_TSYS_PROCESSOR         = "203";
    public static final String DEBIT_VANTIV_PROCESSOR       = "204";
    public static final String DEBIT_WORLDPAY_PROCESSOR     = "205";
    public static final String DEBIT_HEARTLAND_PROCESSOR    = "206";
    public static final String DEBIT_GLOBAL_EMV_PROCESSOR   = "207";
    public static final String DEBIT_GLOBAL_EAST_HOST       = "208";
    public static final String DEBIT_GLOBAL_PAYMENTS        = "209";
    public static final String DEBIT_NOVA_PROCESSOR         = "210";
    public static final String DEBIT_FD_NASHVILLE           = "211";
    public static final String DEBIT_MERCHANT_WAREHOUSE     = "212";
    public static final String DEBIT_FD_BUYPASS_ATL         = "213";
    public static final String DEBIT_ELAVON_PROCESSOR       = "214";
    public static final String DEBIT_JETPAY_PROCESSOR       = "215";
    public static final String DEBIT_FD_RAPID_CONNECT_PROCESSOR = "216";
    public static final String DEBIT_VANTIV_ISO_PROCESSOR       = "217";
    public static final String DEBIT_PAYMENTTECH_PROCESSOR      = "218";
    public static final String DEBIT_FD_TRANSARMOR_PROCESSOR    = "219";
    public static final String DEBIT_CREDIMATIC_PROCESSOR       = "221";
    public static final String DEBIT_MONERIS_PROCESSOR          = "222";
    public static final String DEBIT_FD_TRANSARMOR_CANADA_PROCESSOR = "223";
    public static final String DEBIT_CHASE_ISO_PROCESSOR            = "226";
    public static final String DEBIT_FD_RAPID_CONNECT_CANADA_PROCESSOR  = "227";
    public static final String DEBIT_PLANET_PROCESSOR                   = "228";
    public static final String DEBIT_BORGUN_PROCESSOR                   = "229";
    public static final String DEBIT_CARDNET_UK_PROCESSOR               = "230";
    public static final String DEBIT_GLOBAL_PAYMENTS_UK_PROCESSOR       = "231";
    public static final String DEBIT_COMERCIA_PROCESSOR                 = "233";
    public static final String DEBIT_CHASE_STRATUS_PROCESSOR            = "235";
    public static final String DEBIT_NPP_PROCESSOR                      = "236";
    public static final String DEBIT_WORLDPAY_RAFT_PROCESSOR            = "237";
    public static final String DEBIT_ELAVONISO_PROCESSOR                = "238";
    public static final String DEBIT_AMEX_PROCESSOR                     = "239";
    public static final String DEBIT_FISERV_EUROPE_PROCESSOR            = "243";
    
    
    // DCC Processor ID's(351-400)
    public static final String FEXCO_PROCESSOR              = "351";
    public static final String DCC_PLANET_PROCESSOR         = "352";
    public static final String DCC_FORUMPAY_PROCESSOR       = "353";
    public static final String DCC_SIXX_PAYMENT_PROCESSOR   = "354";
    public static final String OPEN_FX_DCC_PROCESSOR        = "356";


    // Gift Processor ID's (401-600)
    public static final String GIFT_SVS                     = "401";
    public static final String GIFT_WHIZ_GIFT               = "402";
    public static final String GIFT_FDMS                    = "403";
    public static final String GIFT_PAYMENTTECH             = "404";
    public static final String GIFT_HPS_GIFT                = "405";
    public static final String GIFT_STS                     = "406";
    public static final String GIFT_MERCURY                 = "407";
    public static final String GIFT_WORLDPAY_PROCESSOR      = "408";
    public static final String GIFT_E_PROCESSING            = "409";
    public static final String GIFT_VANTIVGIFT              = "410";
    public static final String GIFT_VALUTEC_PROCESSOR       = "411";
    public static final String GIFT_SYNERGY_PROCESSOR       = "412";
    public static final String GIFT_FD_RAPID_CONNECT_PROCESSOR = "413";
    public static final String GIFT_IPSX                    = "414";
    public static final String GIFT_FD_BUYPASS_PROCESSOR    = "415";
    public static final String GIFT_ELAVON_PROCESSOR        = "416";
    public static final String GIFT_INCOMM_PROCESSOR        = "417";
    public static final String GIFT_WAKEFERN_PROCESSOR      = "418";
    public static final String GIFT_GIVEX_PROCESSOR         = "419";
    public static final String GIFT_CLUTCH_PROCESSOR        = "420";
    public static final String GIFT_BLACKHAWK_PROCESSOR     = "421";
    public static final String GIFT_STARGATE_PROCESSOR      = "422";
    public static final String GIFT_SVS_REST                = "423";
    public static final String GIFT_WORLDGIFT_PROCESSOR     = "424";
    public static final String GIFT_CHASE_ISO_PROCESSOR     = "425";
    public static final String GIFT_ECARD_SYSTEM_PROCESSOR  = "426";
    public static final String GIFT_SMARTCLIXX_PROCESSOR    = "427";
    public static final String GIFT_FISERV_CARAT_PROCESSOR  = "428";
    

    // PLCC Processor ID
    public static final String PLCC_DESJARDINS_PROCESSOR            = "601";
    public static final String PLCC_ALLIANCE_DATA                   = "602";
    public static final String PLCC_CAPITAL_ONE                     = "603";
    public static final String PLCC_WELLSFARGO_PROCESSOR            = "604";
    public static final String PLCC_FD_RAPID_CONNECT_PROCESSOR      = "605";
    public static final String PLCC_DATAFAST_PROCESSOR              = "606";
    public static final String PLCC_CAPITAL_ONE_ISO                 = "607";
    public static final String PLCC_FREEDOMPAY_PROCESSOR            = "608";
    public static final String PLCC_SYNCHRONY_PROCESSOR             = "609";
    public static final String PLCC_MEIJER_PROCESSOR                = "610";
    public static final String PLCC_CHASE_PAYMENTECH_PROCESSOR      = "611";
    public static final String PLCC_CHASE_ISO_PROCESSOR             = "612";
    public static final String PLCC_CITI_PROCESSOR                  = "613";
    public static final String PLCC_UATP_PROCESSOR                  = "614";
    public static final String PLCC_STARGATE_PROCESSOR              = "615";

    public static final List<String> PLCC_CAPITAL_ONE_LIST = Arrays.asList(PLCC_CAPITAL_ONE,PLCC_CAPITAL_ONE_ISO);

    // CHECK PROCESSOR ID
    public static final String CHECK_CERTEGY_PROCESSOR              = "702";
    public static final String CHECK_TSYS_PROCESSOR                 = "703";
    public static final String CHECK_FD_RAPID_CONNECT_PROCESSOR     = "704";
    public static final String CHECK_TELECHECK_PROCESSOR            = "705";
    public static final String CHECK_ELAVON_PROCESSOR               = "706";
    public static final String CHECK_WAKEFERN_PROCESSOR             = "707";
    public static final String CHECK_UTA_PROCESSOR                  = "708";
    public static final String CHECK_STARGATE_PROCESSOR             = "709";

    //EBT
    public static final String EBT_FD_RAPID_CONNECT_PROCESSOR       = "651";
    public static final String EBT_FD_TRANSARMOR_PROCESSOR          = "652";
    public static final String EBT_VANTIV_ISO_PROCESSOR             = "653";
    public static final String EBT_CHASE_PROCESSOR                  = "654";
    public static final String EBT_INCOMM_PROCESSOR                 = "656";
    public static final String EBT_CHASE_ISO_PROCESSOR              = "657";
    public static final String EBT_FISERV_PROCESSOR                 = "658";
    public static final String EBT_FD_ONLINE_SNAP_PROCESSOR         = "659";
    public static final String EBT_NPP_PROCESSOR                    = "660";
    public static final String EBT_FORAGE_PROCESSOR                 = "662";
    public static final String FD_ONLINE_EWIC_PROCESSOR             = "663";

    //FLEET
    public static final String FLEET_FD_RAPID_CONNECT_PROCESSOR     = "751";
    public static final String FLEET_FD_TRANSARMOR_PROCESSOR        = "752";
    public static final String FLEET_VANTIV_ISO_PROCESSOR           = "753";
    public static final String FLEET_CHASE_ISO_PROCESSOR            = "754";
    public static final String FLEET_CGI_PROCESSOR                  = "755";
    public static final String FLEET_CHASE_PAYMENTECH_PROCESSOR     = "756";

    //FSA
    public static final String FSA_FD_RAPID_CONNECT_PROCESSOR       = "801";
    public static final String FSA_VANTIV_ISO_PROCESSOR             = "802";
    public static final String FSA_CHASE_PROCESSOR                  = "803";
    public static final String FSA_CHASE_ISO_PROCESSOR              = "804";
    public static final String FSA_NPP_PROCESSOR                    = "805";
    public static final String FSA_CHASESTRATUS_PROCESSOR           = "806";

    //PREPAID
    public static final String PREPAID_FD_RAPID_CONNECT_PROCESSOR   = "851";
    public static final String PREPAID_INCOMM_PROCESSOR             = "852";
    public static final String PREPAID_VANTIV_ISO_PROCESSOR         = "853";
    public static final String PREPAID_CHASE_PAYMENTECH_PROCESSOR   = "854";
    public static final String PREPAID_BLACKHAWK_PROCESSOR          = "856";
    public static final String PREPAID_MBXP_PROCESSOR               = "857";

    //EPP / OTC
    public static final String EPP_INCOMM_PROCESSOR                 = "901";
    public static final String EPP_SOLUTRAN_PROCESSOR               = "902";
    public static final String EPP_VANTIV_ISO_PROCESSOR             = "903";
    public static final String EPP_SODA_HEALTH_PROCESSOR            = "904";
    public static final String EPP_NATION_BENEFIT_PROCESSOR         = "905";
    public static final String EPP_PRENOTE_PROCESSOR                = "906";

    //Third Party API CAll
    public static final String AURUS_PAYGUARD               = "951";
    public static final String CYBERSOURCE_DECISION_MANAGER = "952";
    public static final String KOUNT_DECISION_MANAGER       = "953";
    public static final String TNSPAYMENTS_MERCHANT_TOKEN   = "954";
    public static final String CYBERSOURCE_ECOM_TOKEN       = "955";
    public static final String SFCC                         = "956";
    public static final String AZURE                        = "957";
    public static final String ACCERTIFY                    = "958";
    public static final String FORTER                       = "959";
    public static final String ACI_RED_SHIELD               = "960";
    public static final String FISERV_FRAUD_DECISION_MANGER = "961";
    public static final String RAVELIN_DECISION_MANAGER     = "962";
    public static final String RISKIFIED                    = "963";
    public static final String ACCERTIFY_TOKEN              = "964";
    public static final String SPEED_ECOM_TOKEN             = "965";

    //Loyalty
    public static final String LOYALTY_VELOCITY_PROCESSOR   = "981";
    public static final String LOYALTY_VPROMOS_PROCESSOR    = "982";

    public static final List<String> REFUND_NOT_ALLOW_PROCESSOR = Arrays.asList(GIFT_SVS);
    public static final String UTF = "UTF-8";
    // HSM Token
    public static final int REVERSIBLE_KEYID = 1;
    public static final int IRREVERSIBLE_KEYID = 2;
    public static final String VANTIV_MEIJER_TEST_ZMK_ID = "21";
    public static final String VANTIV_MEIJER_TEST_ZPK_ID = "22";
    public static final String FISERV_EBT_ZPK_ID = "23";
    public static final String FISERV_EBT_ZMK_ID = "24";
    public static final String FISERV_EWIC_ZPK_ID = "25";
    public static final String FISERV_EWIC_ZMK_ID = "26";
    public static final String FDRAPIDHCS_ZPK_ID_TEST = "27";
    public static final String FDRAPIDHCS_ZMK_ID_TEST = "28";
    public static final String CHASEISO_ZMK_ID = "30";
    public static final String CHASEISO_ZPK_ID = "31";
    public static final String FDRAPID_LB_VS_ZPK_ID = "32";
    public static final String FDRAPID_LB_VS_ZMK_ID = "33";
    public static final String FDRAPID_LB_BBW_ZPK_ID = "34";
    public static final String FDRAPID_LB_BBW_ZMK_ID = "35";
    public static final String FDRAPIDHCS_ZPK_ID = "36";
    public static final String FDRAPIDHCS_ZMK_ID = "37";
    public static final String VANTIV_MEIJER_PROD_ZMK_ID = "38";
    public static final String VANTIV_MEIJER_PROD_ZPK_ID = "39";
    public static final int SIXX_PAYMENTS_ZKA_MK_MASTER_KEY_ID_TEST = 40;
    public static final int SIXX_PAYMENTS_ZKA_MK_MASTER_KEY_ID_PROD = 41;
    public static final String CHASEISO_WAKEFERN_ZPK_ID = "42";
    public static final String CHASEISO_WAKEFERN_ZMK_ID = "43";
    public static final int MOBY_DEVICE_P2PE_KEY_ID_TEST = 44;
    public static final int MOBY_DEVICE_P2PE_KEY_ID_PROD = 45;
    public static final int DATECS_DEVICE_P2PE_KEY_ID_TEST = 46;
    public static final int DATECS_DEVICE_P2PE_KEY_ID_PROD = 47;
    public static final int APPLE_CARD_SINGLE_USE_KEY_ZMK_ID_TEST = 49;
    public static final int APPLE_CARD_SINGLE_USE_KEY_ZMK_ID_PROD = 50;
    public static final int APPLE_PIN_SINGLE_USE_KEY_ZMK_ID_TEST = 51;
    public static final int APPLE_PIN_SINGLE_USE_KEY_ZMK_ID_PROD = 52;
    public static final int WISEASY_R1_DEVICE_P2PE_KEY_ID_TEST = 53;
    public static final int WISEASY_R1_DEVICE_P2PE_KEY_ID_PROD = 54;

    public static final String FDRAPID_LB_VS_ZPK_ID_TEST = "59";
    public static final String FDRAPID_LB_VS_ZMK_ID_TEST = "60";
    public static final String FDRAPID_LB_BBW_ZPK_ID_TEST = "61";
    public static final String FDRAPID_LB_BBW_ZMK_ID_TEST = "62";
    public static final String FDRAPID_MEIJER_ZPK = "63";
    public static final String FDRAPID_MEIJER_ZMK = "64";
    public static final String FDRAPID_MEIJER_PROD_ZPK = "65";
    public static final String FDRAPID_MEIJER_PROD_ZMK = "66";

    public static final int IOS18_APPLE_CARD_SINGLE_USE_KEY_ZMK_ID_TEST = 72;
    public static final int IOS18_APPLE_CARD_SINGLE_USE_KEY_ZMK_ID_PROD = 73;
    public static final int IOS18_APPLE_PIN_SINGLE_USE_KEY_ZMK_ID_TEST = 74;
    public static final int IOS18_APPLE_PIN_SINGLE_USE_KEY_ZMK_ID_PROD = 75;
    public static final String VANTIV_KOHLS_TEST_ZMK_ID = "76";
    public static final String VANTIV_KOHLS_TEST_ZPK_ID = "77";
    public static final String VANTIV_KOHLS_PROD_ZMK_ID = "78";
    public static final String VANTIV_KOHLS_PROD_ZPK_ID = "79";
    public static final int ELAVONISO_EUROPE_ZPK_ID = 80;
    public static final int WORLDLINE_ZKA_MK_MASTER_KEY_ID_TEST = 81;
    public static final int WORLDLINE_ZKA_MK_MASTER_KEY_ID_PROD = 82;
    public static final int FISERV_PIN_ENCRYPT_BDK_KEY_ID_TEST  = 83;
    public static final int FISERV_CARD_ENCRYPT_BDK_KEY_ID_TEST = 84;
    public static final int FISERV_CARD_ENCRYPT_BDK_KEY_ID_PROD = 85;
    public static final int FISERV_PIN_ENCRYPT_BDK_KEY_ID_PROD  = 86;
    public static final String FDRAPID_HUDSON_ZMK = "90";
    public static final String FDRAPID_HUDSON_ZPK = "91";
    public static final String FDRAPID_HUDSON_PROD_ZMK = "92";
    public static final String FDRAPID_HUDSON_PROD_ZPK = "93";

    public static final String AURUS_KSN_PIN_BLOCK = "01";
    public static final String CARD_TOKEN_FLAG = "cardTokenFlag";
    public static final String CARD_TOKEN_DATA = "cardData";
    public static final String CARD_TOKEN_KEY = "cardKey";

    public static final String DATA_FROM_TOKEN_FLAG = "reversibleDataFlag";
    public static final String DATA_FROM_TOKEN_DATA = "reversibleData";
    public static final String DATA_FROM_TOKEN_KEY = "reversibleKey";

    public static final String RES_CARD_TOKEN_DATA = "cardTokenData";
    public static final String RES_DATA_FROM_TOKEN = "reversibleData";

    public static final String CALL_FOR_AUTH_RES_CODE="CALFORAUTH";
    public static final String INSUFFICIENT_FUNDS_RES_CODE="INSFUNDS";
    public static final String STRONG_CUSTOMER_AUTHENTICATION_RES_CODE = "SCA";

    public static final String TRANS_TOKEN_EXPIRED = "transTokenExpired";
    public static final String ALREADY_VOIDED      = "alreadyVoided";

    //API VERSION
    public static final String REGIS_VERSION            = "1.1";
    public static final String NMG_VERSION              = "1.2";
    public static final String TARGATE_VERSION          = "1.3";
    public static final String MAURICES_VERSION         = "1.4";
    public static final String ORELLY_VERSION           = "1.5";
    public static final String KUSHKI_VERSION           = "1.6";
    public static final String BESTBUY_VERSION          = "1.7";
    public static final String DRESSBRAN_VERSION        = "1.8";
    public static final String DAVIDS_BRIDAL_VERSION    = "1.9";
    public static final String JUSTICE_US_VERSION       = "1.10";
    public static final String CATHERINES_VERSION       = "1.11";
    public static final String LAN_BRYANT_VERSION       = "1.12";
    public static final String KWI_VERSION              = "1.13";
    public static final String ORPOS_ECOMM_VERSION      = "1.14";
    public static final String JUSTICE_CAN_VERSION      = "1.15";
    public static final String BROOKSTONE_VERSION       = "1.16";
    public static final String MAU_CYBS_VERSION         = "1.17";
    public static final String MARSH_VERSION            = "1.18";
    public static final String TOSHIBA_VERSION          = "1.19";
    public static final String ZUMIEZ_VERSION           = "1.20";
    public static final String GNC_VERSION              = "1.21";
    public static final String PARTECH_VERSION          = "1.22";
    public static final String HUDSON_VERSION           = "1.23";
    public static final String UNITED_RENTAL_VERSION    = "1.24";
    public static final String VFC_VERSION              = "1.25";
    public static final String COSTCO_VERSION           = "1.26";
    public static final String BBI_VERSION              = "1.27";
    public static final String APTOS_VERSION            = "1.28";
    public static final String ALLIED_VERSION           = "1.29";
    public static final String RATNER_VERSION           = "1.30";
    public static final String ANN_TAYLOR_VERSION       = "1.31";
    public static final String ANN_TAYLOR_CAN_VERSION   = "1.32";
    public static final String LOU_AND_GREY_VERSION     = "1.33";
    public static final String LOFT_VERSION             = "1.34";
    public static final String LOFT_CANADA_VERSION      = "1.35";
    public static final String BLUE_PUMP_VERSION        = "1.36";
    public static final String TRADER_JOE_VERSION       = "1.37";
    public static final String WINCO_VERSION            = "1.38";
    public static final String DOLLAR_GENERAL_VERSION   = "1.39";
    public static final String WAKEFERN_VERSION         = "1.40";
    public static final String PENSKE_VERSION           = "1.41";
    public static final String SCHNUCKS_VERSION         = "1.42";
    public static final String VFC_CANADA_VERSION       = "1.43";
    public static final String AFS_VERSION              = "1.44";
    public static final String QU_BEYOND_VERSION        = "1.45";
    public static final String DULUTH_VERSION           = "1.46";
    public static final String LBRANDS_VERSION          = "1.47";
    public static final String SKETCHERS_VERSION        = "1.48";
    public static final String VITAMIN_SHOPPE_VERSION   = "1.49";
    public static final String WARNER_BROTHERS_US_VERSION  = "1.50";
    public static final String WARNER_BROTHERS_UK_VERSION  = "1.51";
    public static final String LUMBER_84_VERSION           = "1.52";
    public static final String DRIVEN_BRANDS_VERSION       = "1.53";
    public static final String HFT_VERSION                 = "1.54";
    public static final String DOVER_VERSION               = "1.55";  //British Petroleum
    public static final String TAILORED_BRAND_VERSION      = "1.56";
    public static final String MONERO_VERSION              = "1.57";
    public static final String VS_LBRANDS_VERSION          = "1.58";  //not is use
    public static final String BBW_LBRANDS_VERSION         = "1.59";
    public static final String AATMAN_VERSION              = "1.60";
    public static final String AT_HOME_VERSION             = "1.61";
    public static final String WEST_MARINE_VERSION         = "1.62";
    public static final String GAP_VERSION                 = "1.63";
    public static final String KROGER_VERSION              = "1.64";
    public static final String PRICE_CHOPPER_VERSION       = "1.65";
    public static final String CUB_FOODS_VERSION           = "1.66";
    public static final String THRYVE_AI_VERSION           = "1.67";
    public static final String VERAS_RETAIL_VERSION        = "1.68";
    public static final String RUE21_VERSION               = "1.69";
    public static final String AURUS_DEFAULT_VERSION_1     = "1.70";
    public static final String AURUS_DEFAULT_VERSION_2     = "1.71";
    public static final String AURUS_DEFAULT_VERSION_3     = "1.72";
    public static final String AERO_POSTALE_VERSION        = "1.73";
    public static final String BELL_HOWELL_VERSION         = "1.74";
    public static final String SNIPES_VERSION              = "1.75";
    public static final String UNDER_ARMOUR_VERSION        = "1.76";
    public static final String MEIJER_VERSION              = "1.77";
    public static final String TOTAL_WINE_VERSION          = "1.78";
    public static final String BLUE_SNAP_VERSION           = "1.79";
    public static final String LULULEMON_VERSION           = "1.80";
    public static final String ROUSES_VERSION              = "1.81";
    public static final String HARRIS_TEETER_VERSION       = "1.82";
    public static final String CLARK_ASSOCIATES_VERSION    = "1.83";
    public static final String PAPA_JHONS_VERSION          = "1.84";
    public static final String BROOKSHIRE_VERSION          = "1.85";
    public static final String EXPRESS_VERSION             = "1.86";
    public static final String UNITED_TEXAS_VERSION        = "1.87";
    public static final String AVAILABLE_VERSION                 = "1.88";
    public static final String TUMI_VERSION                = "1.89";
    public static final String SAMSONITE_VERSION           = "1.90";
    public static final String AAFES_VERSION               = "1.91";
    public static final String RACK_ROOM_VERSION           = "1.92";
    public static final String GORSUCH_VERSION             = "1.93";
    public static final String RED_RIVER_VERSION           = "1.94";
    public static final String LILLY_PULITZER_VERSION      = "1.95";
    public static final String SEPHORA_VERSION             = "1.96";
    public static final String PYRAMID_UK_VERSION          = "1.97";
    public static final String COMER_UK_VERSION            = "1.98";
    public static final String TILLYS_VERSION              = "1.99";
    public static final String UNIVERSAL_ECOM_API_VERSION  = "2.0";
    public static final String UNITED_AIRLINE_VERSION      = "2.1";
    public static final String REED_JEWELLERS_VERSION      = "2.2";
    public static final String AGCO_VERSION                = "2.3";
    public static final String SULLIVAN_FAMILY_CO_VERSION  = "2.4";
    public static final String FRESH_THYME_VERSION         = "2.5";
    public static final String JJILL_VERSION               = "2.6";
    public static final String COLE_HAAN_VERSION           = "2.7";
    public static final String BERGEY_VERSION              = "2.8";
    public static final String UFOT_VERSION                = "2.9";
    public static final String KEY_STONE_VERSION           = "2.10";
    public static final String MASHGIN_VERSION             = "2.11";
    public static final String FASTENAL_VERSION            = "2.12";
    public static final String VAIL_RESORT_VERSION         = "2.13";
    public static final String LAKESHORE_VERSION           = "2.14";
    public static final String NCR_VERSION                 = "2.15";
    public static final String WEGMANS_VERSION             = "2.16";
    public static final String LUNDS_AND_BYERLYS_VERSION   = "2.17";
    public static final String T_MOBILE_VERSION            = "2.18";
    public static final String AGNE_VERSION                = "2.19";
    public static final String CHEDRAUI_VERSION            = "2.20";
    public static final String ECLIPSE_VERSION             = "2.21";
    public static final String NETCOST_MARKET_VERSION      = "2.22";
    public static final String CHICK_FIL_A_VERSION         = "2.23";
    public static final String ITI_VERSION                 = "2.24";
    public static final String CIRCLE_K_VERSION            = "2.25";
    public static final String BUCKLE_VERSION              = "2.26";
    public static final String DAISO_VERSION               = "2.27";
    public static final String CITI_TRENDS                 = "2.28";
    public static final String MICHAEL_KORS_VERSION        = "2.29";
    public static final String FIVE_BELOW_VERSION          = "2.30";
    public static final String LKQ_VERSION                 = "2.31";
    public static final String RACETRAC_VERSION            = "2.32";
    public static final String VERAS_GIII_VERSION          = "2.33";
    public static final String HOME_DEPOT_VERSION          = "2.34";
    public static final String KOHLS_VERSION               = "2.35";
    public static final String SEACRETS_VERSION            = "2.36";
    public static final String WYNSHOP_VERSION             = "2.37";
    public static final String TALBOTS_VERSION             = "2.38";
    public static final String TOTE_VERSION                = "2.39";
    public static final String AVOLTA_VERSION              = "2.40";
    public static final String COCA_COLA_VERSION           = "2.41";
    public static final String OFF_THE_RAX                 = "2.42";
    public static final String EG_AMERICA_TOTE_VERSION     = "2.43";
    public static final String AVOLTA_LATAM_VERSION        = "2.44";
    public static final String SARVATRA_VERSION            = "2.45";
    public static final String PET_SUPERMARKET_VERSION     = "2.46";
    public static final String LOC_VERSION                 = "2.47";
    public static final String AWG_VERSION                 = "2.48";
    public static final String SAKS_GLOBAL_VERSION         = "2.49";
    public static final String AURUS_DEMO_VERSION          = "2.50";
    public static final String SPINX_VERSION               = "2.51";
    public static final String SAVEMART_VERSION            = "2.52";
    public static final String AGBR_VERSION                = "2.53";
    public static final String FOODTASTIC_VERSION          = "2.54";
    public static final String UNFI_VERSION                = "2.55";
    public static final String SWPOS_VERSION               = "2.56";
    public static final String MDI_VERSION                 = "2.57";
    public static final String INSTORE_TECH_VERSION        = "2.58";
    public static final String IN_N_OUT_VERSION            = "2.59";
    public static final String MOUNTAIN_LAKE_VERSION       = "2.60";


    public static final List<String> TOKEN_ECOM_CLIENTS = Arrays.asList(AURUS_DEFAULT_VERSION_3);

    public static final List<String> PROCESS_TRASACTION_MAP = Arrays.asList("0","1","3","4","12","13","123","15","06","16","36","46","126","136","1236","156");
    public static final List<String> ECOM_CLIENTS = Arrays.asList(UNITED_RENTAL_VERSION,ZUMIEZ_VERSION,BBI_VERSION,APTOS_VERSION,ORELLY_VERSION,NMG_VERSION,VFC_VERSION,WAKEFERN_VERSION,GNC_VERSION,VITAMIN_SHOPPE_VERSION,LBRANDS_VERSION,SCHNUCKS_VERSION,HFT_VERSION,AURUS_DEFAULT_VERSION_1,AURUS_DEFAULT_VERSION_2,AURUS_DEFAULT_VERSION_3,DOLLAR_GENERAL_VERSION,DULUTH_VERSION,AATMAN_VERSION,TAILORED_BRAND_VERSION,MAURICES_VERSION,JUSTICE_US_VERSION,LAN_BRYANT_VERSION,QU_BEYOND_VERSION,VS_LBRANDS_VERSION,AT_HOME_VERSION,PRICE_CHOPPER_VERSION,CUB_FOODS_VERSION,THRYVE_AI_VERSION,WEST_MARINE_VERSION,DOVER_VERSION,BROOKSHIRE_VERSION,FRESH_THYME_VERSION,TILLYS_VERSION,UNITED_TEXAS_VERSION,UNITED_AIRLINE_VERSION,LUNDS_AND_BYERLYS_VERSION,CHEDRAUI_VERSION);
    public static final List<String> PAYPAGE_CLIENTS = Arrays.asList(UNITED_RENTAL_VERSION, NMG_VERSION,WAKEFERN_VERSION,GNC_VERSION,ZUMIEZ_VERSION,AT_HOME_VERSION,LBRANDS_VERSION,VS_LBRANDS_VERSION,BBW_LBRANDS_VERSION,PRICE_CHOPPER_VERSION,CUB_FOODS_VERSION,THRYVE_AI_VERSION,AT_HOME_VERSION,DOVER_VERSION,BROOKSHIRE_VERSION,UNITED_TEXAS_VERSION,FRESH_THYME_VERSION,TAILORED_BRAND_VERSION,FRESH_THYME_VERSION,AAFES_VERSION,LUNDS_AND_BYERLYS_VERSION,PARTECH_VERSION,CHEDRAUI_VERSION,WEST_MARINE_VERSION,JJILL_VERSION,UNITED_AIRLINE_VERSION,AURUS_DEFAULT_VERSION_1,ANN_TAYLOR_VERSION,LOFT_VERSION,LAN_BRYANT_VERSION,LKQ_VERSION,VERAS_GIII_VERSION,LILLY_PULITZER_VERSION,VITAMIN_SHOPPE_VERSION,TALBOTS_VERSION,PET_SUPERMARKET_VERSION,UNITED_AIRLINE_VERSION,LULULEMON_VERSION);
    public static final List<String> ANN_VERSION = Arrays.asList(ANN_TAYLOR_VERSION, LOFT_VERSION, LOU_AND_GREY_VERSION);

    public static final List<String> ANN_CANADA_VERSION = Arrays.asList(ANN_TAYLOR_CAN_VERSION, LOFT_CANADA_VERSION);
    public static final List<String> JUSTICE_VERSIONS = Arrays.asList(JUSTICE_US_VERSION, JUSTICE_CAN_VERSION);

    public static final List<String> ORPOS_VERSION = Arrays.asList(DAVIDS_BRIDAL_VERSION, JUSTICE_US_VERSION, JUSTICE_CAN_VERSION, CATHERINES_VERSION, LAN_BRYANT_VERSION, ANN_TAYLOR_VERSION, ANN_TAYLOR_CAN_VERSION, LOU_AND_GREY_VERSION, LOFT_VERSION, LOFT_CANADA_VERSION, APTOS_VERSION, OFF_THE_RAX);
    public static final List<String> CRM_TOKEN_LIST = Arrays.asList(JUSTICE_US_VERSION, JUSTICE_CAN_VERSION, CATHERINES_VERSION, LAN_BRYANT_VERSION, MAURICES_VERSION, DRESSBRAN_VERSION, ANN_TAYLOR_VERSION, ANN_TAYLOR_CAN_VERSION, LOU_AND_GREY_VERSION, LOFT_VERSION, LOFT_CANADA_VERSION, OFF_THE_RAX);
    public static final List<String> POS_ID_VERSION_LIST = Arrays.asList(JUSTICE_US_VERSION, JUSTICE_CAN_VERSION, CATHERINES_VERSION, LAN_BRYANT_VERSION, ANN_TAYLOR_VERSION, LOFT_VERSION, LOU_AND_GREY_VERSION, ANN_TAYLOR_CAN_VERSION, LOFT_CANADA_VERSION, DOLLAR_GENERAL_VERSION, TOSHIBA_VERSION, COSTCO_VERSION);
    public static final List<String> REGISTRATION_NUMBER_VERSION_LIST = Arrays.asList(JUSTICE_US_VERSION, JUSTICE_CAN_VERSION, CATHERINES_VERSION, LAN_BRYANT_VERSION, ANN_TAYLOR_VERSION, LOFT_VERSION, LOU_AND_GREY_VERSION, ANN_TAYLOR_CAN_VERSION, LOFT_CANADA_VERSION,TOSHIBA_VERSION, COSTCO_VERSION);
    public static final List<String> REAUTH_CLIENT_LIST = Arrays.asList(TALBOTS_VERSION,NMG_VERSION,GNC_VERSION,DOLLAR_GENERAL_VERSION,SCHNUCKS_VERSION,VS_LBRANDS_VERSION,BBW_LBRANDS_VERSION,LBRANDS_VERSION,UNDER_ARMOUR_VERSION,WEST_MARINE_VERSION,RUE21_VERSION,LAN_BRYANT_VERSION,AT_HOME_VERSION,TAILORED_BRAND_VERSION,EXPRESS_VERSION,TILLYS_VERSION,LILLY_PULITZER_VERSION,HFT_VERSION,FASTENAL_VERSION,ANN_TAYLOR_VERSION,LOFT_VERSION,VITAMIN_SHOPPE_VERSION,WEGMANS_VERSION,FIVE_BELOW_VERSION,JJILL_VERSION,AURUS_DEFAULT_VERSION_1,RACETRAC_VERSION,LULULEMON_VERSION,MEIJER_VERSION,PET_SUPERMARKET_VERSION,SAKS_GLOBAL_VERSION,KOHLS_VERSION);
    public static final List<String> BTM_LIST = Arrays.asList(MAURICES_VERSION, DRESSBRAN_VERSION);
    public static final List<String> PLAIN_GIFT_CARD_LIST = Arrays.asList(AAFES_VERSION,ANN_TAYLOR_VERSION, ANN_TAYLOR_CAN_VERSION, LOU_AND_GREY_VERSION, LOFT_VERSION, LOFT_CANADA_VERSION,LAN_BRYANT_VERSION,GNC_VERSION);
    public static final List<String> INSTORE_ECOM_CLIENTS = Arrays.asList(MONERO_VERSION, VS_LBRANDS_VERSION, BBW_LBRANDS_VERSION, LBRANDS_VERSION, TAILORED_BRAND_VERSION, HUDSON_VERSION, LILLY_PULITZER_VERSION, UNITED_AIRLINE_VERSION,VAIL_RESORT_VERSION,CIRCLE_K_VERSION,KOHLS_VERSION,AURUS_DEMO_VERSION,SNIPES_VERSION,BUCKLE_VERSION,SAKS_GLOBAL_VERSION,LOC_VERSION);
    public static final List<String> BYPASS_SESSION_TRANS_TYPE_LIST = Arrays.asList(GIFT_BALANCE_INQURY, TRANSACTION_TOKEN, GET_SESSION_TOKEN, GET_PRODUCT_DETAILS, BIN_CHECK, INITIATE, EBT_BALANCE_INQUIRY);
    public static final List<String> BYPASS_SESSION_CLIENT_LIST = Arrays.asList(AT_HOME_VERSION,LBRANDS_VERSION,TAILORED_BRAND_VERSION);
    public static final List<String> IP_ADDRESS_CLIENTS= Arrays.asList("2.0");
    public static final List<String> ATV_PLCC_ALU_CLIENTS = Arrays.asList(NMG_VERSION, LBRANDS_VERSION, BBW_LBRANDS_VERSION, VS_LBRANDS_VERSION, LAN_BRYANT_VERSION, MAURICES_VERSION, RUE21_VERSION, MEIJER_VERSION,AT_HOME_VERSION,JJILL_VERSION,TALBOTS_VERSION,SAKS_GLOBAL_VERSION);
    public static final List<String> OMS_GROCCESARY_API_VERSION = Arrays.asList(WAKEFERN_VERSION, PRICE_CHOPPER_VERSION, CUB_FOODS_VERSION, THRYVE_AI_VERSION,BROOKSHIRE_VERSION,FRESH_THYME_VERSION,UNITED_TEXAS_VERSION,LUNDS_AND_BYERLYS_VERSION,CHEDRAUI_VERSION);
    public static final List<String> PT_CI_WALLET_CLIENT_LIST = Arrays.asList(TAILORED_BRAND_VERSION);
    public static final List<String> BYPASS_OOT_CLIENT_LIST = Arrays.asList(WAKEFERN_VERSION, PRICE_CHOPPER_VERSION, CUB_FOODS_VERSION, THRYVE_AI_VERSION,BROOKSHIRE_VERSION,FRESH_THYME_VERSION,UNITED_TEXAS_VERSION,AAFES_VERSION,LUNDS_AND_BYERLYS_VERSION,CHEDRAUI_VERSION);
    public static final List<String> SUPPORT_L3_ONLINE_PROCESSING = Arrays.asList(LBRANDS_VERSION,BBW_LBRANDS_VERSION,VS_LBRANDS_VERSION);
    public static final List<String> CAI_API_VERSION_LIST = Arrays.asList(SARVATRA_VERSION, BBI_VERSION, ZUMIEZ_VERSION, APTOS_VERSION, BROOKSTONE_VERSION, KWI_VERSION, GNC_VERSION, VITAMIN_SHOPPE_VERSION, DOLLAR_GENERAL_VERSION, LBRANDS_VERSION, SCHNUCKS_VERSION, AURUS_DEFAULT_VERSION_1, AURUS_DEFAULT_VERSION_2, DULUTH_VERSION, AATMAN_VERSION, TAILORED_BRAND_VERSION, MAURICES_VERSION, JUSTICE_US_VERSION, LAN_BRYANT_VERSION, VS_LBRANDS_VERSION, BBW_LBRANDS_VERSION, QU_BEYOND_VERSION, UNDER_ARMOUR_VERSION, WEST_MARINE_VERSION, HUDSON_VERSION, DOVER_VERSION, MEIJER_VERSION, RUE21_VERSION, LULULEMON_VERSION, EXPRESS_VERSION, TILLYS_VERSION, LILLY_PULITZER_VERSION, UNITED_AIRLINE_VERSION, REED_JEWELLERS_VERSION, COLE_HAAN_VERSION, AFS_VERSION, PARTECH_VERSION,FASTENAL_VERSION,LUMBER_84_VERSION,AT_HOME_VERSION,JJILL_VERSION,NCR_VERSION,AAFES_VERSION,CHICK_FIL_A_VERSION,CIRCLE_K_VERSION,RACETRAC_VERSION,WYNSHOP_VERSION,KOHLS_VERSION,TALBOTS_VERSION,AVOLTA_VERSION,PET_SUPERMARKET_VERSION,AURUS_DEMO_VERSION,PAPA_JHONS_VERSION,SAKS_GLOBAL_VERSION,SNIPES_VERSION,BUCKLE_VERSION,LOC_VERSION,MOUNTAIN_LAKE_VERSION,ANN_TAYLOR_VERSION,LOFT_VERSION);
    //public static final List<String> CI_GENERATION_IN_CHILD_TXN_CLIENT_LIST = Arrays.asList(SARVATRA_VERSION, MAURICES_VERSION, JUSTICE_US_VERSION, LAN_BRYANT_VERSION, VS_LBRANDS_VERSION, QU_BEYOND_VERSION, BBW_LBRANDS_VERSION, LBRANDS_VERSION, TAILORED_BRAND_VERSION, HUDSON_VERSION, UNDER_ARMOUR_VERSION, MEIJER_VERSION, RUE21_VERSION, LULULEMON_VERSION, SCHNUCKS_VERSION, TILLYS_VERSION, LILLY_PULITZER_VERSION, UNITED_AIRLINE_VERSION, REED_JEWELLERS_VERSION, EXPRESS_VERSION, PARTECH_VERSION,FASTENAL_VERSION,LUMBER_84_VERSION,AT_HOME_VERSION,JJILL_VERSION,NCR_VERSION,AAFES_VERSION,CHICK_FIL_A_VERSION,CIRCLE_K_VERSION,RACETRAC_VERSION,WYNSHOP_VERSION,KOHLS_VERSION,TALBOTS_VERSION,AVOLTA_VERSION,PET_SUPERMARKET_VERSION,AURUS_DEMO_VERSION,PAPA_JHONS_VERSION,SAKS_GLOBAL_VERSION,SNIPES_VERSION,BUCKLE_VERSION,DOLLAR_GENERAL_VERSION,LOC_VERSION,MOUNTAIN_LAKE_VERSION,ANN_TAYLOR_VERSION,LOFT_VERSION);
    public static final List<String> CI_GENERATION_IN_CHILD_TXN_CLIENT_LIST = Arrays.asList(BBI_VERSION, ZUMIEZ_VERSION, APTOS_VERSION, BROOKSTONE_VERSION);
    public static final List<String> BLOCK_PROCESSOR_TOKEN_CLIENT_LIST = Arrays.asList(EXPRESS_VERSION, ANN_TAYLOR_VERSION, LOFT_VERSION);
    public static final List<String> FINAL_POSTAUTH_COUNT_CLIENT_LIST = Arrays.asList(FASTENAL_VERSION, WEGMANS_VERSION, JJILL_VERSION,RACETRAC_VERSION,MEIJER_VERSION,SAKS_GLOBAL_VERSION);
    public static final List<String> CLIENT_INITIATED_REAUTH_LIST = Arrays.asList(VS_LBRANDS_VERSION, BBW_LBRANDS_VERSION, LBRANDS_VERSION, UNDER_ARMOUR_VERSION, TAILORED_BRAND_VERSION, EXPRESS_VERSION, LILLY_PULITZER_VERSION, ORELLY_VERSION, NMG_VERSION, JJILL_VERSION, WEST_MARINE_VERSION, AURUS_DEFAULT_VERSION_1,LULULEMON_VERSION,WEGMANS_VERSION,SAKS_GLOBAL_VERSION);

    // SUB TRANS TYPE
    public static final String CLIENTELE_TENDER_ADD_SUB_TRANS_TYPE          = "3";
    public static final String DEBIT_EXCHANGE_SUB_TRANS_TYPE                = "4";
    public static final String CREDIT_EXCHANGE_SUB_TRANS_TYPE               = "5";
    public static final String PLCC_PAYMENT_CASH_SUB_TRANS_TYPE             = "6";
    public static final String PLCC_PAYMENT_GIFT_SUB_TRANS_TYPE             = "7";
    public static final String PLCC_PAYMENT_CHECK_SUB_TRANS_TYPE            = "8";
    public static final String PLCC_PAYMENT_DEBIT_SUB_TRANS_TYPE            = "12";
    public static final String PLCC_PASSCODE_SUB_TRANS_TYPE                 = "15";
    public static final String SEND_TAKE_SUB_TRANS_TYPE                     = "17";
    public static final String ONE_DOLLAR_SUB_TRANS_TYPE                    = "18";
    public static final String DEFFERED_BY_PROCESSOR                        = "19";
    public static final String PROCESSOR_REFRAL_SUB_TRANS_TYPE              = "20";
    public static final String DO_NOT_CHARGE_CARD_SUB_TT                    = "23";
    public static final String NEUTRAL_KEYED_REFERRAL_SUB_TRANS_TYPE        = "24";
    public static final String PLCC_PAYMENT_TRAVELER_CHECK_SUB_TRANS_TYPE   = "25";
    public static final String PLCC_PAYMENT_CREDIT_SUB_TRANS_TYPE           = "26";
    public static final String PLCC_PAYMENT_MONEY_ORDER_SUB_TRANS_TYPE      = "27";
    public static final String RECURRING_SUB_TT                             = "28";
    public static final String UTILITY_RETRY_SUB_TRANS_TYPE                 = "29";
    public static final String SUBSCRIPTION_SUB_TRANS_TYPE                  = "30";
    public static final String REDEBAN_MULTI_COMPRA                         = "31";
    public static final String ORDER_IMPORT_SUB_TRANS_TYPE                  = "32";
    public static final String WALLET_SUB_TRANS_TYPE                        = "33";
    public static final String REAUTH_SUB_TRANS_TYPE                        = "34";
    public static final String PROC_SNF_SUB_TRANS_TYPE                      = "35";
    public static final String TAVE_ENC_SUB_TRANS_TYPE                      = "36";
    public static final String PREAUTH_SUB_TRANS_TYPE                       = "37";
    public static final String AUTH_SUB_TRANS_TYPE                          = "38";
    public static final String PRECHECK_SALE_SUB_TRANS_TYPE                 = "39";
    public static final String DCC_REFUND_SUB_TRANS_TYPE                    = "40";
    public static final String CHECK_OVERRIDE_SUB_TRANS_TYPE                = "41";
    public static final String PRESCREEN_PROTO_SUB_TRANS_TYPE               = "42";
    public static final String PRESCREEN_ACCEPT_SUB_TRANS_TYPE              = "43";
    public static final String CHECK_CASHING_SUB_TRANS_TYPE                 = "44";
    public static final String MADE_OFFER_SUB_TRANS_TYPE                    = "45";
    public static final String IPN_STATUS_SUB_TRANS_TYPE                    = "46";
    public static final String AURUS_REPROCESS_SUB_TRANS_TYPE               = "47";
    public static final String MOTO_SUB_TRANS_TYPE                          = "48";
    public static final String ORDER_LOOKUP_SUB_TRANS_TYPE                  = "49";
    public static final String RE_TENDER_SUB_TRANS_TYPE                     = "50";
    public static final String ACCOUNT_INQUIRY_SUB_TRANS_TYPE               = "51";
    public static final String RETRY_SUB_TRANS_TYPE                         = "52";
    public static final String ONLY_CARD_VALIDATION_SUB_TT                  = "53";
    public static final String CHECK_VERIFY_SUB_TRANS_TYPE                  = "54";
    public static final String FRAUD_INQUIRY_UPDATE_SUB_TRANS_TYPE          = "55";
    public static final String BACKORDER_FRAUD_SUB_TRANS_TYPE               = "56";
    public static final String UPLOAD_PRODUCT_DATA_SUB_TRANS_TYPE           = "57";
    public static final String CORP_SALES_AND_APPEASEMENTS_SUB_TRANS_TYPE   = "58";
    public static final String CI_GENERATION_SUB_TRANS_TYPE                 = "59";
    public static final String RE_SUBMISSION_SUB_TRANS_TYPE                 = "60";
    public static final String DELAYED_CHARGES_SUB_TRANS_TYPE               = "61";
    public static final String NO_SHOW_SUB_TRANS_TYPE                       = "62";
    public static final String INSTALLMENT_SUB_TRANS_TYPE                   = "63";
    public static final String INCREMENTAL_SUB_TRANS_TYPE                   = "64";
    public static final String ESTIMATE_SUB_TRANS_TYPE                      = "65";
    public static final String KP_SESSION_SUB_TRANS_TYPE                    = "66";
    public static final String UPDATE_ORDER_CANCEL_SUB_TRANS_TYPE           = "67";
    public static final String CARD_ACTIVATION_MODE                         = "68";
    public static final String PRECHECK_POA_SUB_TRANS_TYPE                  = "69";
    public static final String ECOMM_SUB_TRANS_TYPE                         = "70";
    // reserve sub transaction type 71-85 for Petro Implementation as per CCT req
    public static final String POSTPAY_GRR_SUB_TRANS_TYPE                   = "86";
    public static final String CANCEL_LOYELTY_SUB_TRANS_TYPE                = "87";
    public static final String SNF_APPROVED_SUB_TRANS_TYPE                  = "88";
    public static final String SNF_UTILITY_SUB_TRANS_TYPE                   = "89";
    public static final String THREE_DS_SUPPORTED_VERSION_SUB_TRANS_TYPE    = "90";
    public static final String THREE_DS_AUTH_REQUEST_SUB_TRANS_TYPE         = "91";
    public static final String THREE_DS_CHALLENGE_REQUEST_SUB_TRANS_TYPE    = "92";
    public static final String THREE_DS_ABANDONED_REQUEST_SUB_TRANS_TYPE    = "93";
    public static final String THREE_DS_NPA_SUB_TRANS_TYPE                  = "94";
    public static final String TOKEN_CERT_SUB_TRANS_TYPE                    = "95";
    public static final String PROVISION_CERT_SUB_TRANS_TYPE                = "96";
    public static final String UNSCHEDULED_MIT_WITH_COF                     = "97";
    public static final String SCHEDULED_MIT_WITH_COF_VAR_AMT               = "98";
    public static final String SCHEDULED_MIT_WITH_COF_FIX_AMT               = "99";
    public static final String UNSCHEDULED_CIT_WITH_COF                     = "100";
    public static final String SCHEDULED_CIT_WITH_COF_VAR_AMT               = "101";
    public static final String SCHEDULED_CIT_WITH_COF_FIX_AMT               = "102";
    public static final String ORDER_LOOKUP_FOR_PREFERRED_CARD              = "103";
    public static final String RISK_INQUIRY_FOR_REFUND_SUB_TT               = "104";
    public static final String DEVICE_FINGERPRINT_REQUEST_SUB_TT            = "105";
    public static final String RISK_INQUIRY_EXCHANGE_SUB_TT                 = "106";
    public static final String SALE_SUB_TRANS_TYPE                          = "107";
    public static final String REFUND_SUB_TRANS_TYPE                        = "108";
    public static final String PAYMENT_LOYELTY_SUB_TRANS_TYPE               = "109";
    public static final String VMC_SUB_TRANS_TYPE                           = "110";
    public static final String CLIENT_ID_REQ_SUB_TRANS_TYPE                 = "111";
    public static final String TTP_SAF_DELETION_TOKEN                       = "112";
    public static final String FINAL_COLLECTION_SUB_TRANS_TYPE              = "113";
    public static final String PRENOTE_UPDATE_SUB_TRANS_TYPE                = "114";
    public static final String PRENOTE_DELETE_SUB_TRANS_TYPE                = "115";
    public static final String DCC_RATE_CHECKING_SUB_TRANS_TYPE             = "116";
    public static final String CI_CARD_ID_SUB_TRANS_TYPE                    = "117";
    public static final String EWIC_STATUS_SUB_TRANS_TYPE                   = "118";
    public static final String BALANCE_HISTORY_SUB_TRANS_TYPE               = "119";
    public static final String EGC_SUB_TRANS_TYPE = "120";

    // UTILITY SUB TRANSACTION TYPES
    public static final String READ_TIMEOUT_UTILITY_SUB_TRANS_TYPE                  = "901";
    public static final String DUP_REF_TRANS_REPROCESS_UTILITY_SUB_TRANS_TYPE       = "902";
    public static final String ORPHAN_UTILITY_SUB_TRANS_TYPE                        = "903";
    public static final String SNF_UTILITY_SUB_TT                                   = "904";
    public static final String BATCH_REFUND_PROCESSING_UTILITY_SUB_TRANS_TYPE       = "905";
    public static final String PROCESSOR_TOKEN_GEN_SUB_TRANS_TYPE                   = "906";
    public static final String TOR_RETRY_UTILITY_SUB_TRANS_TYPE                     = "907";
    public static final String ADS_SNF_UTILITY_SUB_TRANS_TYPE                       = "908";
    public static final String REAUTH_UTILITY_SUB_TRANS_TYPE                        = "909";
    public static final String PARTIAL_TXN_PROCESSING_UTILITY_SUB_TRANS_TYPE        = "910";
    public static final String PRESALE_PROCESSING_UTILITY_SUB_TRANS_TYPE            = "911";


    public static final String PLCC_PASSCODE_APPROVAL_CODE="728PC";
    public static final String NEUTRAL_CARD_APPROVAL_CODE="625NC";

    public static final String ROAM_PAY_EMV_SWIPER_TYPE     ="14";
    public static final String SWIPER_TYPE_VERIFONE         ="16";
    public static final String SWIPER_TYPE_VERIFONE_CCT     ="17";
    public static final String INFINIA_SWIPER_TYPE          ="18";
    public static final String BB_IUP250_SWIPER_TYPE        ="19";
    public static final String DATECS_BLUELITE_SWIPER_TYPE  ="20";
    public static final String AXIUM_SWIPER_TYPE            ="21";
    public static final String M7_DEVICE_SWIPER_TYPE        ="22";    // live with Aurus HSM
    public static final String M4_DEVICE_SWIPER_TYPE        ="23";    // CK HSM
    public static final String ELO_SWIPER_TYPE              ="24";



    public static final List<String> NMG_SPCL_RESPONSE = Arrays.asList("208", "211");

    public static final String TRANSACTION_SERVICE = "transactionService";
    public static final String PROCESS_TRANSACTION_SERVICE = "processTransactionService";
    public static final String COMMIT_TRANSACTION_SERVICE = "commitTransactionService";
    public static final String CHECK_TRANSACTION_STATUS_SERVICE = "checkTransactionStatusService";
    public static final String NO_DATA = "NO_DATA";
    public static final String NULL = "null";
    public static final String EMV_FULL= "1";
    public static final String EMV_PARTIAL= "2";
    public static final String CVV_PROMPT= "1";
    public static final String CVV_NOT_PROMPT= "0";
    public static final String TIMEOUT= "10000";
    public static final String FOR_PROCESS = "FOR_PROCESS";
    public static final String FOR_COMMIT = "FOR_COMMIT";
    public static final int PROCESS_TRANSACTION_TIMER_IN_PROGRESS = 1;
    public static final int PROCESS_TRANSACTION_TIMER_EXPIRED = 2;
    public static final int TRANSACTION_TIMER_IN_PROGRESS = 3;
    public static final int TRANSACTION_TIMER_EXPIRED = 4;
    public static final int TRANSACTION_TIMER_STOPPED = 5;
    public static final int TRANSACTION_TIMER_SUSPENDED = 6;
    public static final int DISCLAIMER_TRANSACTION_TIMER = 7;
    public static final int PROCESS_TRANSACTION_TIMER_DURATION = 90;
    public static final int TRANSACTION_TIMER_DURATION = 60;
    public static final String PROCESSOR_TOKEN_TYPE_VALUE = "CARD_TOKEN";
    public static final String VERSION_ATT = "v116";
    public static final String SEQUENCE_NUMBER_ATT = "1";
    public static final String TOTAL_LN_SEQUENCE_NUMBER_ATT = "1";
    public static final String SOURCE_SYSTEM_ATT = "TARGET";
    public static final String SOURCE_LOCATION_ATT = "CS";
    public static final String SOURCE_USER_ATT = "TARGET";
    public static final String MODE_ATT = "5";
    public static final String RECORD_FORMAT_ATT = "R";
    public static final String CONVERSATION_ID_ATT = "cc1655_IDD123";
    public static final String ORIGINATORS_IP = "124.30"+".211.126";


    public static final String COUNTRY_CHILE = "Chile";
    public static final String COUNTRY_PERU ="Peru";

    //credit card type for nmg
    public static final String NMG_VISA = "VISA";
    public static final String NMG_MASTERCARD = "MC";
    public static final String NMG_AMEX = "AMEX";
    public static final String NMG_DISCOVER = "DISC";
    public static final String NMG_DINER = "DIN";
    public static final String NMG_JCB = "JCB";
    public static final String SUCCESS = "SUCCESSFUL";
    public static final String FAILED = "FAILED";
    public static final String NMG_PLCC_BG = "BG";
    public static final String NMG_PLCC_NM = "CHG";

    public static final String RECEIPT_DATA_INSERT_TEXT = "RECEIPT DATA IS INSERTED";
    public static final String RECEIPT_DATA_UPADATE_TEXT = "RECEIPT DATA IS UPADATED";
    public static final String RECEIPT_DATA_DELETE_TEXT = "RECEIPT DATA IS DELETED";
    public static final String SURVEY_DATA_INSERTED_TEXT = "SURVEY DATA IS INSERTED";
    public static final String PRODUCT_DATA_INSERTED_TEXT = "PRODUCT DATA IS INSERTED";
    public static final String PARTIAL_APPROVED_TEXT = "AP";

    //SUB TRANS TYPE
    public static final String NMG_DISCOUNT = "12";
    public static final String NMG_REWARD_PURCHASE = "13";
    public static final String PLCC_BG = "BG";
    public static final String PLCC_NM = "NM";
    public static final String PLCC_BGC = "BGC";
    public static final String PLCC_NMC = "NMC";
    public static final String POSITIVE_CARD_INDICATOR = "0";
    public static final String NEGATIVE_CARD_INDICATOR = "1";
    public static final String NEUTRAL_CARD_INDICATOR = "2";

    //Form Factor Code
    public static final String BACK_OFFICE_PORTAL       = "1";
    public static final String BANK_PORTAL              = "2";
    public static final String CONSUMER_PORTAL          = "3";
    public static final String ISO_PORTALS              = "4";
    public static final String IVR                      = "5";
    public static final String POS_HYPERCOM             = "6";
    public static final String POS_INGENICO             = "7";
    public static final String POS_LIPMAN               = "8";   // Digital Payments
    public static final String POS_MICROS               = "9";
    public static final String POS_VERIFONE             = "10";
    public static final String MERCHANT_PORTAL          = "11";
    public static final String MOBILE_APPS              = "12";
    public static final String VIRTUAL_TERMINAL         = "13";
    public static final String WEB                      = "14";
    public static final String POS_MX                   = "15";
    public static final String TABLET                   = "16";
    public static final String AESDK                    = "17";
    public static final String ENAGE_DEVICE             = "20";
    public static final String PAX_DEVICE               = "21";
    public static final String MOBY_DEVICE              = "22";
    public static final String EQUINOX_DEVICE           = "23";
    public static final String WISEASY_R1_DEVICE        = "24";
    public static final String QUALCOMM_DEVICE          = "25";
    public static final String DATECS_DEVICE            = "26";
    public static final String ELO_DEVICE               = "27";
    public static final String VERIFONE_ANDROID         = "28";
    public static final String OPT_TERMINL_TYPE         = "29";
    public static final String CASTLE_ANDROID           = "30";
    public static final String KOZEN_DEVICE             = "31";
    public static final String MOREFUN_DEVICE           = "32";
    public static final String AISINO_DEVICE            = "33";
    public static final String LANDI_ANDROID            = "34";


    public static final String DEFAULT_MASKED_IP = "XX.XX.XX.XX";
    public static final String HARDCODE_CARD_IDEN_KEY = "PR@1!K35WQOPASD$JS6FL57FC8IL343HJKJS";
    public static final String HCS_FLAG = "0";
    /*public static final String USE_INFINISPAN_FLAG = "1";
    public static final String USE_HSM_FLAG = "1";
    public static final String USE_ON_GUARD_FLAG = "1";*/
    public static final int COMERCIA_CARD_ENCRYPT_LMK_KEY_ID_TEST = 7;
    public static final int COMERCIA_MAC_LMK_KEY_ID_TEST = 8;
    public static final int GLOBAL_CARD_ENCRYPT_LMK_KEY_ID_TEST = 12;
    public static final int GLOBAL_CARD_ENCRYPT_LMK_KEY_ID_PROD = 18;
    public static final int COMERCIA_CARD_ENCRYPT_LMK_KEY_ID_PROD = 43;
    public static final int COMERCIA_MAC_LMK_KEY_ID_PROD = 44;
    public static final int CIRCLEK_MAC_KEY_ID_TEST = 70;
    public static final int CIRCLEK_MAC_KEY_ID_PROD = 70;


    public static final String FDGIFT_PROGRAM_IDENTIFIER = "Blackhawk";

    public static final String TRANSACTION_TOKEN_VALIDITY = "1800000";
    public static final List<String> ALLOW_CRM_TOKEN = Arrays.asList(CREDIT_SALE, CREDIT_REFUND, DEBIT_SALE, DEBIT_REFUND, PLCC_ORDER, PLCC_RETURN, PLCC_BALANCE_INQUIRY,PLCC_ACCOUNT_LOOKUP,PLCC_PAYMENT_PROTOCOL);
    //SUB TRANS TYPE
    public static final String ERROR_KI_SYSTEM_TRANS_EXTRA_FLAG = "4";
    //Ascena output token value
    public static final String eComTokenOnly = "1";
    public static final String crmTokenOnly = "2";
    public static final String eComCrmTokenBoth = "0";
    public static final String processorTokenOnly = "3";
    public static final String crmProcessorTokenBoth = "4";
    public static final String crmPlainPlccCardBoth = "5";
    public static final String thirdPartyBalanceInquiry= "6";
    public static final String ciTokenOnly= "7";
    public static final List<String> GENERATE_CRM_TOKEN_FLAG = Arrays.asList(crmTokenOnly,eComCrmTokenBoth,crmProcessorTokenBoth,crmPlainPlccCardBoth);
    public static final List<String> BUYPASS_INFINISPAN_CALL = Arrays.asList(ECOM_TOKEN_TRANS_CODE,SP_ACCOUNT_KI_API_CODE,SP_UPDATE_CARD_INFO_API_CODE,TRANSACTION_STATUS);
    public static final List<String> API_VERSION_PLCC_RESPONSE = Arrays.asList(DRESSBRAN_VERSION,NMG_VERSION,BROOKSTONE_VERSION);
    //Kushki API key
    public static final String KUSHKI_RECORD_API_KEY = "424ddd92-5f30-4610-8990-30b5b02f16a5";

    public static final String HCS_SETTLEMENT_STATUS = "99";
    public static final String TCS_SETTLEMENT_STATUS = "0";
    public static final String NON_FINANCIAL_SETTLE_STATUS = "98";
    public static final String FINANCIAL_NOT_SETTLE_STATUS = "97";

    public static final String TIMEOUT_REVERSAL_IDENTIFIER = "T";
    public static final String INTERNAL_REVERSAL_REASON_CODE = "202";

    public static final String TIMEOUT_VOID_IDENTIFIER = "V";
    public static final String INTERNAL_VOID_REASON_CODE = "203";

    public static final List<String> REVERSAL_REASON_CODE_B = Arrays.asList("901","902","903","904","905","906","907","908","909");
    public static final List<String> CAPITALONE_CLIENT_VERSION = Arrays.asList("1.10","1.4","1.8");
    public static final List<String> EXTRA_PRODUCT_API_VERSION = Arrays.asList(ORELLY_VERSION,ZUMIEZ_VERSION,BLUE_PUMP_VERSION,BBI_VERSION,APTOS_VERSION,DRIVEN_BRANDS_VERSION,SCHNUCKS_VERSION,DOVER_VERSION,MONERO_VERSION,LBRANDS_VERSION,VS_LBRANDS_VERSION,BBW_LBRANDS_VERSION,WEST_MARINE_VERSION,MEIJER_VERSION,RED_RIVER_VERSION,FASTENAL_VERSION,LAN_BRYANT_VERSION,CIRCLE_K_VERSION,LUMBER_84_VERSION,EG_AMERICA_TOTE_VERSION,HOME_DEPOT_VERSION,AVOLTA_VERSION,ANN_TAYLOR_VERSION,LOFT_VERSION);
    //R&F
    public static final String RNF_FIRST_LEVEL_CALL         ="1";
    public static final String RNF_SECOND_LEVEL_CALL        ="2";
    public static final String RNF_THIRD_LEVEL_CALL         ="3";

    public static final String RNF_DEFAULT_FLAG             ="0";
    public static final String RNF_IGNORE_FLAG              ="1";
    public static final String RNF_DENY_FLAG                ="2";
    public static final String RNF_PROCESS_FLAG             ="3";
    public static final String RNF_NOT_PROCESSED_FLAG       ="4";


    // Transaction Status
    public static final String TXN_STATUS_APPROVAL = "1";
    public static final String TXN_STATUS_DECLINE = "2";
    public static final String TXN_STATUS_BOTH = "3";

    public static final String USE_INFINISPAN_FLAG = "1";
    public static final String AVS_MATCH = "M";

    // Rule ID's
    public static final String IP_FILTER                    = "1";
    public static final String MONTHLY_VOLUME_LIMIT         = "2";
    public static final String SALE_AMT_BELOW               = "3";
    public static final String SALE_AMT_ABOVE               = "4";
    public static final String REFUND_AMT_ABOVE             = "6";
    public static final String DAY_TXN_AMOUNT               = "7";
    public static final String HOURS_TXN_COUNT              = "8";
    public static final String DAY_TXN_COUNT                = "9";
    public static final String DAY_IVR_TXN_COUNT            = "10";
    public static final String MISMATCH_ADDRESS_ZIP         = "11";
    public static final String MISMATCH_ZIP                 = "12";
    public static final String MISMATCH_ADDRESS             = "13";
    public static final String MISMATCH_CVV                 = "14";
    public static final String REFUND_ON_DIFF_CARD          = "15";
    public static final String REFUND_WITHOUT_SALE          = "16";
    public static final String REFUND_ON_GIFT_CARD          = "17";
    public static final String REFUND_IN_CASH               = "18";
    public static final String MULTI_DECLINE_ON_TXN         = "19";
    public static final String DUPLICATE_TXN                = "20";
    public static final String BIN_RANGE_FILTER             = "21";
    public static final String AURUS_TOKEN_FILTER           = "23";
    public static final String PROCESSOR_TOKEN_FILTER       = "24";
    public static final String MERCHANT_TOKEN_FILTER        = "25";



    //CARDHOLDER_ID_CODE
    public static final String CREDIT_CARDHOLDER_ID         = "1";
    public static final String DEBIT_CARDHOLDER_ID          = "2";
    public static final String DEBIT_DUKPT_CARDHOLDER_ID    = "3";
    public static final String GIFT_CARDHOLDER_ID           = "4";
    public static final String PLCC_CARDHOLDER_ID           = "5";
    public static final String CHECK_CARDHOLDER_ID          = "6";
    public static final String EBT_CARDHOLDER_ID            = "7";
    public static final String FLEET_CARDHOLDER_ID          = "8";
    public static final String FSA_CARDHOLDER_ID            = "9";
    public static final String PREPAID_CARDHOLDER_ID        = "10";
    public static final String EPP_CARDHOLDER_ID            = "11";
    public static final String LOYALTY_CARDHOLDER_ID        = "12";
    public static final String UPI_CARDHOLDER_ID            = "13";
    public static final String VOUCHER_CARDHOLDER_ID        = "14";
    public static final String CASH_CARDHOLDER_ID           = "15";

    //KUSHKI WRAPPER MODEL
    public static final String RECORD_API_QA                = "qa";
    public static final String RECORD_API_UAT               = "uat";
    public static final String RECORD_API_STAGING           = "stg";
    public static final String RECORD_API_PRODUCTION        = "prod";

    public static final List<String> NMG_KI_TRANS_CODE_LIST = Arrays.asList("107","117");//,"116","208","211"); //add tender //hetch ki auth
    public static final String PARTIAL_VOID_IDENTIFIER = "P";

    public static final List<String> EXPIRE_SESSION_TRANS_LIST = Arrays.asList(PROCESSOR_CARD_TOKEN,CANCEL,GET_SESSION_TOKEN);

    // Wallet's Type
    public static final String VISA_CHECK_OUT       = "1";
    public static final String MASTER_PASS          = "2";
    public static final String AMEX_SAFE_KEY        = "3";
    public static final String PAYPAL               = "4";
    public static final String ALIPAY               = "5";
    public static final String SHOP_RUNNER          = "6";
    public static final String APPLEPAY             = "7";
    public static final String CHASEPAY             = "8";
    public static final String WECHAT               = "9";
    public static final String AMAZONPAY            = "10";
    public static final String GOOGLEPAY            = "11";
    public static final String CUP                  = "12";
    public static final String AFTERPAY             = "13";
    public static final String KLARNAPAY            = "14";
    public static final String VENMO                = "15";
    public static final String SEZZLE               = "16";
    public static final String FLEXA                = "17";
    public static final String SAMSUNGPAY           = "18";
    public static final String FORUMPAY             = "19";
    public static final String TRUSTLY              = "20";
    public static final String AFFIRM               = "21";
    public static final String OTTPAY               = "22";
    public static final String OXXOPAY              = "23";
    public static final String SAFETYPAY            = "24";
    public static final String INTERAC_IN_APP       = "25";
    public static final String MERCADOPAGO          = "26";
    public static final String SYNCHRONY            = "27";
    public static final String GLN                  = "28";
    public static final String PAZE                 = "29";
    public static final String KOTAK_UPI            = "30";
    public static final String SHOPPAY              = "31";
    public static final String UPSIDE               = "32";
    public static final String ALIPAY_PLUS          = "33";
    public static final String UPI_INDIA            = "34";
    public static final String PAYCO                = "35";
    public static final String LINE_PAY             = "36";
    public static final String RAKUTEN_PAY          = "37";
    public static final String NETS                 = "38";
    public static final String GRABPAY              = "39";
    public static final String SHOPEEPAY            = "40";
    public static final String ATOME                = "41";
    public static final String TOSS                 = "42";
    public static final String LDOT_PAY             = "43";
    public static final String LG_PAY               = "44";
    public static final String SPEI                 = "45";
    public static final String OXXO                 = "46";
    public static final String UBP                  = "47";
    public static final String PAYMAYA              = "48";
    public static final String CASH_APP             = "49";
    public static final String OZOW                 = "50";
    public static final String M_PESA               = "51";
    public static final String ALIPAY_CN            = "52";
    public static final String TRUEMONEY            = "53";
    public static final String ALIPAY_HK            = "54";
    public static final String TNG                  = "55";
    public static final String GCASH                = "56";
    public static final String KAKAOPAY             = "57";
    public static final String NAVERPAY             = "58";
    public static final String TOSSPAY              = "59";
    public static final String OCBC                 = "60";
    public static final String CHANGIPAY            = "61";
    public static final String MPAY                 = "62";
    public static final String BLUECODE             = "63";
    public static final String HELLOMONEY_BY_AUB    = "64";
    public static final String TINABA               = "65";
    public static final String HIPAY                = "66";
    public static final String KASPIKZ              = "67";
    public static final String BIGPAY_MY            = "68";
    public static final String BIGPAY_TH            = "69";
    public static final String BIGPAY_SG            = "70";
    public static final String KPLUS                = "71";
    public static final String ZALOPAY              = "72";
    public static final String BILLEASE             = "73";
    public static final String BOOST                = "74";
    public static final String BPI                  = "75";
    public static final String ALIPAY_MO            = "76";
    public static final String DANA                 = "77";
    public static final String DOKU                 = "78";
    public static final String KREDIVO_ID           = "79";
    public static final String K_PLUS               = "80";
    public static final String MAYA                 = "81";
    public static final String NAVERPAY_GOF         = "82";
    public static final String MYPB                 = "83";
    public static final String PAYNOW               = "84";
    public static final String PAYPAY               = "85";
    public static final String QRIS                 = "86";
    public static final String RABBIT_LINE_PAY      = "87";
    public static final String GRABPAY_SG           = "88";
    public static final String AKULAKU_PAYLATER     = "89";
    public static final String AKULAKU_PAYLATER_PH  = "90";
    public static final String UK_BANK_PAYMENT      = "91";
    public static final String EU_LBANK_PAYMENT     = "92";
    public static final String TEST_WALLET          = "93";
    public static final String HYDEPAY              = "94";
    public static final String SPIRE_ACH            = "95";
    public static final String BVNK                 = "96";
    public static final String KPN                  = "97";
    public static final String LPAY                 = "98";
    public static final String CITCON_UPI           = "99";
    public static final String ZIP_PAY              = "100";
    public static final String TRUEMED             = "111";
    
    public static final List<String> WALLET_TYPE_LIST = Arrays.asList(VISA_CHECK_OUT, MASTER_PASS, AMEX_SAFE_KEY, PAYPAL, SHOP_RUNNER, APPLEPAY, CHASEPAY, AMAZONPAY, GOOGLEPAY, ALIPAY, WECHAT, AFTERPAY, CHASEPAY, KLARNAPAY, VENMO, SEZZLE, FLEXA, SAMSUNGPAY, TRUSTLY, AFFIRM, FORUMPAY, OXXOPAY, MERCADOPAGO, PAZE, SHOPPAY, CASH_APP, ZIP_PAY, TRUEMED);
    public static final List<String> ALTERNATE_PAY_PROC_LIST = Arrays.asList(PAYPAL, ALIPAY, WECHAT, AMAZONPAY, AFTERPAY, VENMO, KLARNAPAY, SEZZLE, FLEXA, TRUSTLY, AFFIRM, FORUMPAY, OXXOPAY, CUP, SAFETYPAY, MERCADOPAGO, SHOPPAY, UPSIDE, ALIPAY_CN, TRUEMONEY, ALIPAY_HK, TNG, GCASH, KAKAOPAY, NAVERPAY, TOSSPAY, OCBC, CHANGIPAY, MPAY, HELLOMONEY_BY_AUB, ZALOPAY, HIPAY, KASPIKZ, BIGPAY_MY, BIGPAY_SG, BIGPAY_TH, K_PLUS, BILLEASE, BOOST, BPI, DANA, KREDIVO_ID, MAYA, MYPB, RABBIT_LINE_PAY, GRABPAY_SG, AKULAKU_PAYLATER, AKULAKU_PAYLATER_PH, TEST_WALLET, ZIP_PAY, TRUEMED,BVNK);
    public static final List<String> WALLET_PROC_LIST = Arrays.asList(VISA_CHECK_OUT, MASTER_PASS, AMEX_SAFE_KEY, SHOP_RUNNER, APPLEPAY, CHASEPAY, SAMSUNGPAY);
    public static final List<String> PT_CI_WALLET_LIST = Arrays.asList(PAYPAL);
    public static final List<String> KLARNA_VCN_CLIENTS = Arrays.asList(EXPRESS_VERSION, LAN_BRYANT_VERSION, ORELLY_VERSION,LOFT_VERSION,ANN_TAYLOR_VERSION);
    public static final List<String> ECOM_CITCON_PAY_PROC_LIST = Arrays.asList(PAYPAL, VENMO, KLARNAPAY, OXXO, OXXOPAY, SPEI, MERCADOPAGO, WECHAT, ALIPAY,CUP,PAYCO, NAVERPAY,KAKAOPAY ,LINE_PAY, PAYPAY,RAKUTEN_PAY, ALIPAY_PLUS, PAYNOW, NETS,GRABPAY, SHOPEEPAY, ATOME, ALIPAY_HK,DANA, GCASH, RABBIT_LINE_PAY, TNG,TRUEMONEY, BPI, BOOST, TOSS, LDOT_PAY, SAMSUNGPAY, UBP, PAYMAYA, CASH_APP,  AFTERPAY, OZOW,M_PESA);

    // Wallet's Sub Type
    public static final String CREATE_PAYMENT_API          =   "1";   // PAYER ID
    public static final String PAYPAL_BAID_API             =   "2";
    public static final String PAYPAL_SOAP_API             =   "3";
    public static final String PAYPAL_V2_ORDER_API         =   "4";    //paypal v2
    public static final String PAYPAL_V1_ORDER_API         =   "5";    //paypal v1 NVP conversion
    public static final String OTTPAY_SUB_WALLET_TYPE      =   "6";
    public static final String PAYPAL_BRAINTREE            =   "7";
    public static final String PAYPAL_PAYFAST              =   "8";
    public static final String PAYPAL_V2_VAULT_ORDER_API   =   "10";
    public static final String SEZZLE_V1_CARDSESSION_API   =   "1";
    public static final String KLARNA_CUST_TOKEN_API_FLOW  =   "3";
    public static final String KLARNA_VIRTUAL_CARD_FLOW    =   "4";
    public static final String MASTERCARD_SRC_SUB_WALLET   =   "2";

    // BO Wallet's Sub Type
    public static final String DIRECT                       =   "0";
    public static final String VIA_CITCON                   =   "1";
    public static final String VIA_PAYER_ID                 =   "5";
    public static final String VIA_SOAP_API                 =   "6";
    public static final String VIA_V1_ORDER_API             =   "7";
    public static final String VIA_V2_ORDER_API             =   "8";
    public static final String VIA_BRAINTREE                =   "9";
    public static final String VIA_OTTPAY                   =   "6";

    // Input/Output Matrix
    public static final String CARD_DATA_INPUT              = "1000";
    public static final String CARD_IDENTIFIER_INPUT        = "0100";
    public static final String GET_CARD_BIN_INPUT           = "0010";
    public static final String PROCESSOR_TOKEN_INPUT        = "0001";
    public static final String SESSION_ID_BASED_GET_TOKEN   = "00001";
    public static final String GET_CARD_BIN_OUTPUT          = "100000";
    public static final String GET_ACTUAL_CARD_OUTPUT       = "200000";
    public static final String GET_CARD_IDENTIFIER_OUTPUT   = "010000";
    public static final String GET_MERCHANT_TOKEN_OUTPUT    = "001000";

    public static final String INVALID_EXPIRY_CODE= "018";
    public static final String INVALID_EXPIRY_MSG= "INVALID CARD EXPIRY";

    //Amex Secret key Wallet's
    public static final String AMEX_SHARED_SECRET_KEY      =  "QgOd3hOkAeWEr3OdJGgdSLMWhxDBIDWS";

    public static final List<String> WALLET_ID_LIST = Arrays.asList(AMEX_SAFE_KEY,MASTER_PASS,VISA_CHECK_OUT,SHOP_RUNNER,APPLEPAY,GOOGLEPAY);

    //Penske
    public static final List<String> NO_CALLBACK_URL_TRANSACTION_TYPE = Arrays.asList(Constants.TRANSACTION_STATUS,Constants.CREDIT_VOID, Constants.CREDIT_REFUND, Constants.CREDIT_POSTAUTH, Constants.CREDIT_REVERSAL, Constants.DEBIT_VOID, Constants.DEBIT_REFUND, Constants.DEBIT_REVERSAL);

    //Industry Code
    public static final String REST_INDUSTRY        =  "2";
    public static final String RETAIL_INDUSTRY      =  "4";
    public static final String ECOMM_INDUSTRY       =  "5";
    public static final String MOTO_INDUSTRY        =  "7";
    public static final String PETRO_INDUSTRY       =  "8";
    public static final String SUPER_MARKET_INDUSTRY=  "9";
    public static final String AUTO_RENTAL_INDUSTRY =  "10";
    public static final String AIRLINES_INDUSTRY    =  "11";

    //EBT Indicator
    public static final String EBT_CASH = "1";
    public static final String EBT_SNAP = "2";
    public static final String EBT_EWIC = "3";
    public static final String EBT_CASH_FOOD = "4";

    //PREPAID NETWORK INDICATOR
    public static final String BLACK_HAWK   = "1";
    public static final String INCOMM       = "2";
    public static final String GIFT_PROG_ID = "3";
    public static final String LOTTERY      = "4";
    public static final String VOUCHER      = "5";
    public static final String OTC          = "6";
    public static final String MBXP         = "7";

    //OUTDOOR INDICATOR
    public static final String INDOOR       = "0"; // or Blank
    public static final String OUTDOOR      = "1";
    public static final String CAR_IQ       = "3";
    public static final String CAR_WASH     = "4";
    public static final String EV_CHARGING  = "5";

    //Transaction Source
    public static final String STPL               = "1";
    public static final String INTERNAL_USE       = "2";
    public static final String CPOC_APPLICATION   = "3";
    public static final String REAUTH_UTILITY     = "4";
    public static final String APPLE_CARD_READER  = "5";
    public static final String REPROCESSING_BY_UTILITY   = "6";
    public static final String TXN_WO_MAC_VAL     = "7";
    public static final String MPOC_IOS_APP       = "8";
    public static final String MPOC_ANDROID_APP   = "9";
    public static final String ECST               = "10";
    public static final String AATMAN             = "11";
    public static final String DS                 = "12";
    public static final String ACTWS              = "13";


    //Is Test Flag
    public static final String TEST_ENVIRONMENT = "Y";

    public static final String NMG_BGR_BIN = "60902500";
    public static final String NMG_BGS_BIN = "6090250089";
    public static final String NMG_BGC_BIN = "609025009";
    public static final String NMG_NMR_BIN = "60902600";
    public static final String NMG_NMS_BIN = "6090260089";
    public static final String NMG_NMC_BIN = "609026009";
    public static final String NMG_BGS1_BIN = "6090250069";
    public static final String NMG_NMS1_BIN = "6090260069";
    public static final List<String> NMG_SUB_CARD_TYPE_LIST = Arrays.asList("BGR", "BGS", "BGC", "NMR", "NMS", "NMC");

    public static final List<String> DB_NAMES_DEBUG=Arrays.asList("PRIMARY_MARIADB_DB_DEBUG","SECONDARY_MARIADB_DB_DEBUG",
            "PRIMARY_STOREPLACE_DB_DEBUG","PRIMARY_STOREPLACE_VPN_DB_DEBUG","SECONDARY_STOREPLACE_DB_DEBUG","SECONDARY_STOREPLACE_VPN_DB_DEBUG",
            "PRIMARY_POSTGRES_DB_DEBUG","SECONDARY_POSTGRES_DB_DEBUG");

    public static final List<String> EMV_DB_NAMES_DEBUG=Arrays.asList("EMV_PRIMARY_POSTGRES_DB_DEBUG","EMV_SECONDARY_POSTGRES_DEBUG","EMV_PRIMARY_MARIADB_DB_DEBUG","EMV_SECONDARY_MARIADB_DB_DEBUG");
    public static final List<String> EMV_STPL_DB_NAMES_DEBUG=Arrays.asList("EMV_STPL_PM_DB_DEBUG","EMV_STPL_SC_DB_DEBUG","EMV_STPL_PM_VPN_DB_DEBUG","EMV_STPL_SC_VPN_DB_DEBUG");

    //Kount changes
    public static final List<String> KOUNT_TRANSACTION_TYPE = Arrays.asList(Constants.CREDIT_SALE,Constants.CREDIT_PREAUTH,Constants.CREDIT_ZERO_DOLLAR);

    public static final List<String> BILLING_ISO_LIST = Arrays.asList("6.1", "6.2", "6.3", "6.4", "6.5", "6.6", "6.7", "6.8", "6.9", "6.10", "6.11", "6.17", "6.18", "6.19", "6.20", "6.21", "6.22", "6.23", "6.24", "6.25", "6.26", "6.27", "6.28", "6.29");
    public static final List<String> UAT_NODE_ID_LIST = Arrays.asList("91","92","93","94","95","96","97","98","99");

    public static final List<String> BILLING_NAME_ISO_LIST = Arrays.asList("6.1", "6.2", "6.3");

    //hitmap
    public static final List<String> HITMAP_TRANS_LIST = Arrays.asList("125","102","130","1","2","3","4","5","6","8","107","132","201","202","203","204","205","206","146","150");

    //ecom transaction list
    public static final List<String> ECOM_TRANS_LIST = Arrays.asList("125","108","3","23","57","203","243","263","4","24","58","204","244","264","128","107","102","111","115","132","44","135","143","221","229","142","150");

    //ecom state insert transaction list
    public static final List<String> ECOM_STATE_TRACK_INSERT_TRANS_LIST = Arrays.asList("125","108");

    //ecom state insert transaction list
    public static final List<String> ECOM_STATE_TRACK_DELETE_TRANS_LIST = Arrays.asList("3","23","57","203","243","263","4","24","58","204","244","264");

    //ecom update/get transaction list
    public static final List<String> ECOM_STATE_TRACK_UPDATE_TRANS_LIST = Arrays.asList("128","107","102","111","115","132","44","135","143","221","229","142");

    //Rental
    public static final List<String> RENTAL_ISO_LIST = Arrays.asList("19.1","19.2","19.3","19.4","19.5","19.6","19.7","19.8","19.9","19.10","19.11","19.12","19.13","19.14","19.15","19.16","19.17","19.18");

    public static final List<String> ES01_ECOM_CLIENTS = Arrays.asList(ZUMIEZ_VERSION,BBI_VERSION,APTOS_VERSION,NMG_VERSION,GNC_VERSION,VITAMIN_SHOPPE_VERSION,MAURICES_VERSION,LAN_BRYANT_VERSION,TAILORED_BRAND_VERSION,HFT_VERSION,SCHNUCKS_VERSION,SKETCHERS_VERSION,UNDER_ARMOUR_VERSION,DULUTH_VERSION);
    public static final List<String> ES02_ECOM_CLIENTS = Arrays.asList(WAKEFERN_VERSION);

    public static final List<String> ES01_41_SWITCH_ID = Arrays.asList("47","48","59","60","49","50","61","62","67","68","69","70");
    public static final List<String> ES02_42_SWITCH_ID = Arrays.asList("112","113","118","119","114","115","116","117");
    public static final List<String> ES03_43_SWITCH_ID = Arrays.asList("120","121","122","123","124","125","126","127","160","161","162","163");
    public static final List<String> ES04_44_SWITCH_ID = Arrays.asList("128","129","130","131","132","133","134","135");
    public static final List<String> ES05_45_SWITCH_ID = Arrays.asList("144","145","146","147","148","149","150","151");
    public static final List<String> ES36_66_SWITCH_ID = Arrays.asList("168","169","170","171","172","173","174","175");
    public static final List<String> ECOM_NODE_LIST = Stream.of(
            ES01_41_SWITCH_ID,
            ES02_42_SWITCH_ID,
            ES03_43_SWITCH_ID,
            ES04_44_SWITCH_ID,
            ES05_45_SWITCH_ID,
            ES36_66_SWITCH_ID,
            UAT_NODE_ID_LIST
    ).flatMap(List::stream).collect(Collectors.toList());

    public static final List<String> OMNI_CHANNEL_CLIENTS = Arrays.asList(GNC_VERSION,KWI_VERSION,TAILORED_BRAND_VERSION,DULUTH_VERSION);
    public static final List<String> OMNI_CHANNEL_WITH_PAR_REF_CLIENTS = Arrays.asList("1.99");

    public static final List<String> EMV_NODE_DIRPOLL_EXCLUDE_NONFIANCE_TRANS_LIST = Arrays.asList("125","128","150");
    public static final List<String> TRANS_ANALYTICS_FINANCIAL_API_LIST = Arrays.asList(MEIJER_VERSION);
    public static final List<String> TRANS_ANALYTICS_FINANCIAL_TRANS_LIST = Arrays.asList("1","2","3","4","5","6","21","22","23","24","25","26","42","43","48","51","53","57","58","91","92","95","96","201","202","203","204","205","206","207","221","222","225","226","228","241","242","243","244","245","246","261","262","263","264","265","266","282","284","285","286","301","302","305","306");

    public static final int STATIC_NUMERIC_INT = 11111;
    public static final String STATIC_ALPHA_NUMERIC = "AAAAA";
    public static final String STATIC_ENC_ALPHA_NUMERIC = "CBIYPRBrkoVMdLsT7fnnuw==";

    public static final List<String> NF_TXN_LIST=Arrays.asList(KEY_EXCHANGE_CODE,CORP_KEY_EXCHANGE_CODE,CASH_REFAUND,SIGNATURE,CARD_TOKEN,TRANSACTION_TOKEN,RECEIPT_UPLOAD,SURVEY_UPLOAD,PRODUCT_UPLOAD,CUSTOMER_REGISTRATION_CODE,GET_CARD_BIN_CODE,PROCESS_TRANS,COMMIT_TRANS,CHECK_STATUS_TRANS,GET_PRODUCT_DETAILS,
            GET_CAP_KEYS,GET_CARD_DATA_CODE,CANCEL,TRANSACTION_STATUS,SP_ACCOUNT_KI_API_CODE,
            SP_UPDATE_CARD_INFO_API_CODE,PROCESSOR_CARD_TOKEN,CLOSE_TRANSACTION,SETTLEMENT_CODE,ECOM_TOKEN_TRANS_CODE,UPDATE_AURUS_IVR_RECORD,
            TRANSACTION_HISTORY,TRANS_HISTORY_CODE,GENERATE_SESSION_ID,UPDATE_CARD_EXPIRY_CODE,GET_SUBSCRIPTION_DETAILS,SESSION_STATUS_TRANS_CODE,
            DCC_RATE,BILLING_TOKEN,THIRD_PARTY_TOKEN,GET_SESSION_TOKEN,MERCHANT_AUTH_API,UPDATE_BILLING_SHIPPING_DETAILS,REQUEST_TOKEN,BIN_CHECK,INITIATE,ACTIVE_TERMINAL,REGISTER_AMOUNT_UPDATE,IPN,
            CUSTOMER_DATA_REMOVAL,GET_CONFIGURATION,UPDATE_TOKEN,PAYMENT_PAGE_RENDERATION,CRM_OPERATIONS,RISK_INQUIRY,RISK_ACTION_STATUS,EXPRESS_PAY_ORDER,DECISION_UPDATE,START_PAYMENT,TRANSACTION_LOOKUP,MTLS_CERT_DOWNLOAD,CLIENT_CREDENTIALS_LOOKUP);

    public static final List<String> PREPAID_IND_NOT_SUPPORTED_LIST= Arrays.asList(APTOS_VERSION,UNDER_ARMOUR_VERSION);
    public static final String UNIVERSAL_BASIC_ECOM_API_VERSION = "2.0";
    public static final String UNKNOWN_API_VERSION = "1.0";

    public static final List<String> HPCC_FINANCIAL_TRANS_LIST = Arrays.asList("1", "21", "91", "221", "241", "261", "282", "301", "2", "22", "43", "53", "92", "222", "242", "262", "284", "302", "3", "23", "57", "243", "263", "4", "24", "58", "244", "264", "5", "25", "48", "95", "225", "245", "265", "285", "305", "42", "201", "202", "203", "204", "205", "206", "207", "209", "210", "211", "6", "26", "51","50", "96", "226", "246", "266", "286", "306", "228", "63", "48", "285");
    public static final List<String> HPCC_NON_FINANCIAL_LIST = Arrays.asList("125","128","132","141", "288", "289", "145", "111", "143", "231", "142", "135", "136", "137", "138", "139", "140", "0", "8", "214", "9", "29", "44", "212", "224", "229", "307", "41", "281", "45", "46", "47", "50","51", "52", "55", "54", "59", "287", "97", "101", "102", "107", "108", "115", "116", "119", "120", "122", "127", "129", "130", "208", "227", "230", "283", "310", "123", "157", "291", "48", "285","311","150");

    public static final List<String> VANTIVISO_PROCESSOR_LIST = Arrays.asList(CREDIT_VANTIV_ISO_PROCESSOR,DEBIT_VANTIV_ISO_PROCESSOR,EBT_VANTIV_ISO_PROCESSOR,GIFT_VANTIVGIFT,FSA_VANTIV_ISO_PROCESSOR,PREPAID_VANTIV_ISO_PROCESSOR,FLEET_VANTIV_ISO_PROCESSOR);
    public static final List<String> ADS_SNF_SUPPORTED_LIST = Arrays.asList(LAN_BRYANT_VERSION,ANN_TAYLOR_VERSION,LOFT_VERSION,MAURICES_VERSION,TALBOTS_VERSION);

    public static final List<String> PROCESSOR_FAILOVER_CLIENTS = Arrays.asList(Constants.WAKEFERN_VERSION,Constants.CIRCLE_K_VERSION);

    public static final List<String> THREE_DS_CLIENT_LIST = Arrays.asList(LUNDS_AND_BYERLYS_VERSION, CUB_FOODS_VERSION, LKQ_VERSION);

    public static final List<String> DEBIT_NETWORK_LIST = Arrays.asList("A5","1","2","3","03","5","7","11","12","13","14","15","16","17","18","19","20","21","23","25","26","28","29","30","31","33","34","35","36","37","39","41","42","43","46","47","48","49","51","52","56","57","59","62","63","64","65","67","68","69");
    public static final List<String> DEBIT_PINLESS_INDICATOR_LIST = Arrays.asList("A","B","C","D","E","L","P");
    public static final List<String> RETRY_REVERSAL_PROCESSOR_LIST  = Arrays.asList(Constants.PLCC_CITI_PROCESSOR,Constants.FSA_CHASE_ISO_PROCESSOR,Constants.FLEET_CHASE_ISO_PROCESSOR,Constants.EBT_CHASE_ISO_PROCESSOR,Constants.CREDIT_CHASE_ISO_PROCESSOR,Constants.DEBIT_CHASE_ISO_PROCESSOR,Constants.CREDIT_VANTIV_ISO_PROCESSOR,Constants.DEBIT_VANTIV_ISO_PROCESSOR,Constants.EBT_VANTIV_ISO_PROCESSOR,Constants.GIFT_VANTIVGIFT,Constants.FSA_VANTIV_ISO_PROCESSOR,Constants.PREPAID_VANTIV_ISO_PROCESSOR,Constants.EPP_VANTIV_ISO_PROCESSOR,Constants.FLEET_VANTIV_ISO_PROCESSOR,Constants.CREDIT_NPP_PROCESSOR, Constants.DEBIT_NPP_PROCESSOR,Constants.EBT_NPP_PROCESSOR,Constants.FSA_NPP_PROCESSOR,Constants.GIFT_INCOMM_PROCESSOR,Constants.PREPAID_INCOMM_PROCESSOR,Constants.EPP_INCOMM_PROCESSOR,Constants.CREDIT_ELAVONISO_PROCESSOR,Constants.DEBIT_ELAVONISO_PROCESSOR);


    //Fiserv Mastercard
    public static final String MERCHANT_PAY_GATEWAY  = "00000226414";

    public static final List<Integer> XML_PROCESSOR_LIST = Arrays.asList(30,33,45,48,216,219,223,227,413,605,704,651,652,751,752,801,851,3);
    public static final List<Integer> ISO_PROCESSOR_LIST = Arrays.asList(32,47,217,226,612,653,657,753,754,802,804,853,903);
    public static final List<String> IDEMPOTENCY_TRANSACTION_SUPPORT_LIST  = Arrays.asList(Constants.CREDIT_SALE,Constants.CREDIT_REFUND,Constants.CREDIT_PREAUTH,Constants.CREDIT_POSTAUTH,Constants.CREDIT_VOID,Constants.CREDIT_REVERSAL,Constants.CREDIT_RE_AUTH,Constants.GIFT_ACTIVATION,Constants.GIFT_REDEEM,Constants.GIFT_RETURN,Constants.GIFT_BALANCE_TRANSFER,Constants.GIFT_DEACTIVAT_WITHREFUND,Constants.GIFT_DEACTIVAT_WITHOUTREFUND,Constants.GIFT_VOID,Constants.GIFT_RELOAD,Constants.GIFT_REVERSAL,Constants.GIFT_ISSUE,Constants.MERCHANDISE_RETURN,Constants.GIFT_CASHOUT,Constants.GIFT_ISSUE_VIRTUAL_CARD,Constants.GIFT_BALANCE_MERGE,Constants.GIFT_PRE_AUTH,Constants.GIFT_POST_AUTH,Constants.GIFT_SALE_ACTIVE_CODE,Constants.PLCC_ORDER,Constants.PLCC_RETURN,Constants.PLCC_PREAUTH,Constants.PLCC_POSTAUTH,Constants.PLCC_VOID,Constants.PLCC_REVERSAL,Constants.PLCC_PROCESSOR_CARD_TOKEN,Constants.PLCC_RE_AUTH,Constants.DEBIT_SALE,Constants.DEBIT_REFUND,Constants.DEBIT_PREAUTH,Constants.DEBIT_POSTAUTH,Constants.DEBIT_VOID,Constants.DEBIT_REVERSAL,CHECK_AUTHORIZATION,Constants.CHECK_REFUND,Constants.CHECK_VOID,Constants.CHECK_REVERSAL,Constants.CHECK_ADJUSTMENT,Constants.EBT_ORDER,Constants.EBT_RETURN,Constants.EBT_VOID,Constants.EBT_REVERSAL,Constants.EBT_VOUCHER_CLEAR,Constants.FLEET_ORDER,Constants.FLEET_RETURN,Constants.FLEET_PREAUTH,Constants.FLEET_POSTAUTH,Constants.FLEET_VOID,Constants.FLEET_REVERSAL,Constants.FSA_ORDER,Constants.FSA_RETURN,Constants.FSA_PREAUTH,Constants.FSA_POSTAUTH,Constants.FSA_VOID,Constants.FSA_REVERSAL,Constants.PREPAID_ACTIVATION,Constants.PREPAID_SALE,Constants.PREPAID_DEACTIVATION,Constants.PREPAID_REFUND,Constants.PREPAID_VOID,Constants.PREPAID_REVERSAL,Constants.PREPAID_SALE_ACTIVE_CODE,Constants.PREPAID_RELOAD,Constants.PREPAID_POR_REFUND,Constants.PREPAID_CASHOUT,Constants.PREPAID_DISBURSEMENT,Constants.EPP_SALE,Constants.EPP_REFUND,Constants.EPP_VOID,Constants.EPP_REVERSAL,Constants.EPP_REDEEM);

    public static final List<String> BLUEFIN_DECRYPTX_NOT_REACHABLE_ERROR_CODE = Arrays.asList("1101","1102","1103","1104","1105","1305");
    public static final List<String> BLUEFIN_DECRYPTION_FAILED_CODE_ERROR_CODE = Arrays.asList("1001","1201","1202","1203","1204","1301","1302","1303","1401","1402","1403","1404","1405","1406");

    public static final List<String> DECRYPTION_FAILED = Arrays.asList("1001.00", "1201.00", "1202.00", "1203.00", "1204.00", "1301.00", "1302.00", "1303.00", "1401.00", "1402.00", "1403.00", "1404.00", "1405.00", "1406.00");
    public static final List<String> NOT_REACHABLE_LIST= Arrays.asList("11.01","11.02" ,"11.03" ,"11.04", "11.05" ,"13.05");
    public static final List<String> PRE_REFUND_TRANS_LIST = Arrays.asList("16", "30", "218", "98","232");

    public static final List<String> EBT_ON_IFRAME_FOR_WORLDPAY_CLIENT_LIST = Arrays.asList("2.37", "2.16");
    public static final List<String> EBT_ON_IFRAME_FOR_FD_CLIENT_LIST = Arrays.asList("2.37","1.77");
    public static final List<String> EBT_ON_IFRAME_FOR_FORAGE_CLIENT_LIST = Arrays.asList("1.39");



    private Constants() {

    }
}