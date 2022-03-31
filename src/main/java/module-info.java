module com.app.cloudstorage.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires io.netty.all;
    requires org.apache.logging.log4j;

    opens com.app.cloudstorage.client to javafx.fxml;
    exports com.app.cloudstorage.client;
}