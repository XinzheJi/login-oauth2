package com.system.ai.predict.taos;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaosQueryResult {
    private String status;
    private Integer code;
    private String desc;
    private List<String> columns = new ArrayList<>();
    private List<List<Object>> data = new ArrayList<>();
    private Integer rows;

    public static TaosQueryResult fromJson(JsonNode root) {
        TaosQueryResult r = new TaosQueryResult();
        if (root == null || root.isNull()) {
            return r;
        }
        r.status = text(root, "status");
        if (root.has("code")) {
            r.code = root.get("code").isInt() ? root.get("code").asInt() : null;
        }
        if (root.has("desc")) {
            r.desc = root.get("desc").asText("");
        }

        if (root.has("head") && root.get("head").isArray()) {
            for (JsonNode n : root.get("head")) {
                r.columns.add(n.asText());
            }
        } else if (root.has("column_meta") && root.get("column_meta").isArray()) {
            for (JsonNode n : root.get("column_meta")) {
                if (n.isArray() && n.size() > 0) {
                    r.columns.add(n.get(0).asText());
                }
            }
        }

        if (root.has("data") && root.get("data").isArray()) {
            for (JsonNode row : root.get("data")) {
                List<Object> list = new ArrayList<>();
                for (JsonNode c : row) {
                    if (c.isNumber()) {
                        list.add(c.numberValue());
                    } else if (c.isBoolean()) {
                        list.add(c.booleanValue());
                    } else if (c.isNull()) {
                        list.add(null);
                    } else {
                        list.add(c.asText());
                    }
                }
                r.data.add(list);
            }
        }
        if (root.has("rows")) {
            r.rows = root.get("rows").asInt();
        }
        return r;
    }

    private static String text(JsonNode n, String f) {
        return n.has(f) ? n.get(f).asText() : null;
    }
}

