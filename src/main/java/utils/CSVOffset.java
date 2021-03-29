package utils;

import org.apache.spark.sql.connector.read.streaming.Offset;

import com.google.gson.Gson;

public class CSVOffset extends Offset {

    private int offset;

    public CSVOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public String json() {

        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }
}