package info.lusito.mapeditor.utils;

import java.util.function.BiPredicate;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class TableViewFilter<T> implements InvalidationListener {

    private final TextField text;
    private final TableView table;
    private final ObservableList<T> filteredItems = FXCollections.observableArrayList();
    private final ObservableList<T> originalItems;
    private final BiPredicate<T, String> filter;

    public TableViewFilter(TextField text, TableView table, ObservableList<T> originalItems, BiPredicate<T, String> filter) {
        this.text = text;
        this.table = table;
        this.originalItems = originalItems;
        this.filter = filter;
        text.textProperty().addListener(this);
        invalidated(text.textProperty());
    }

    @Override
    public void invalidated(Observable o) {
        if (text.textProperty().get().isEmpty()) {
            table.setItems(originalItems);
            return;
        }

        final String lowerText = text.textProperty().get().toLowerCase();

        filteredItems.clear();
        for (T item : originalItems) {
            if (filter.test(item, lowerText)) {
                filteredItems.add(item);
            }
        }

        table.setItems(filteredItems);
    }
}
