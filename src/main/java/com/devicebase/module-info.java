module com.devicebase.sdk {
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.datatype.jsr310;

    exports com.devicebase;
    exports com.devicebase.client;
    exports com.devicebase.exception;
    exports com.devicebase.http;
    exports com.devicebase.model;
    exports com.devicebase.websocket;
}