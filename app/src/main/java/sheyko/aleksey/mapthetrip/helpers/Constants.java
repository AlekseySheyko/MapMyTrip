package sheyko.aleksey.mapthetrip.helpers;

public class Constants {

    public class Device {
        public static final String MODEL_NUMBER = "1";
        public static final String SYSTEM_NAME = "Android";
        public static final String SOFTWARE_VERSION = "1.0";
        public static final String USER_ID = "1";
        public static final String USER_DEFINED_TRIP_ID = "";
        public static final String REFERENCE_NUMBER = "";
        public static final String ENTITY_ID = "1";
    }

    public class Trip {
        public class Status {
            public static final String RESUME = "Resume";
            public static final String PAUSE = "Pause";
            public static final String FINISH = "Finish";
        }
    }

    public class Timer {
        public class Commands {
            public static final int PAUSE = 0;
            public static final int STOP = 1;
        }
    }

    public class Map {
        public static final int UPDATE_INTERVAL = 5 * 1000;
    }

    public class ActionBar {
        public class Tab {
            public static final int GAS = 1;
            public static final int REST = 2;
        }
    }
}
