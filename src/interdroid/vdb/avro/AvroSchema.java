package interdroid.vdb.avro;


import org.apache.avro.Schema;

public class AvroSchema {

    private AvroSchema() {
        // No Construction;
    }

    public static final String NAMESPACE = "interdroid.vdb.content.avro.schemas";
    public static final Schema SCHEMA;
    public static final Schema RECORD;
    public static final String RECORD_DEFINITION = "Record";

    // TODO: It would be nice to support cross namespace records and such.
    // TODO: Extract constants

    static {
        // Taken from proposed schema for schemas:
        String schema =
            "{\"type\": \"record\", \"name\": \"Type\", \"namespace\": \"interdroid.vdb.content.avro.schemas\","
            +"\n "
            +"\n \"fields\": ["
            +"\n     {\"name\": \"type\", \"type\": ["
            +"\n         {\"type\": \"record\", \"name\": \"Record\","
            +"\n          \"fields\": ["
            +"\n              {\"name\": \"name\", \"type\": \"string\", \"ui.label\": \"Record Name\", \"ui.required\" : \"true\", \"ui.list\": \"true\"},"
            +"\n              {\"name\": \"doc\", \"type\": \"string\"},"
            +"\n              {\"name\": \"namespace\", \"type\": \"string\", \"ui.list\": \"true\", \"ui.label\": \"Application Name\"},"
            +"\n              {\"name\": \"aliases\", \"type\": {\"type\": \"array\", \"items\": {\"type\": \"string\"}}},"
            +"\n              {\"name\": \"fields\","
            +"\n               \"type\": {\"type\": \"array\", \"items\":"
            +"\n                        {\"type\": \"record\", \"name\": \"FieldDef\","
            +"\n                         \"fields\": ["
            +"\n                             {\"name\": \"name\", \"type\": \"string\"},"
            +"\n                             {\"name\": \"label\", \"type\": \"string\"},"
            +"\n                             {\"name\": \"doc\", \"type\": \"string\"},"
            +"\n                             {\"name\": \"list\", \"ui.label\": \"Show In List\", \"type\": \"boolean\"},"
            +"\n                             {\"name\": \"aliases\", \"type\": {\"type\": \"array\", \"items\": {\"type\": \"string\"}}},"
            +"\n                             {\"name\": \"type\", \"type\": \"Type\"},"
/*
            +"\n                             ,{\"name\": \"defaultValue\", \"type\":"
            +"\n                              {\"type\": \"record\", \"name\": \"JsonValue\","
            +"\n                               \"fields\": ["
            +"\n                                   {\"name\": \"value\","
            +"\n                                    \"type\": ["
            +"\n                                        \"long\", \"double\", \"string\","
            +"\n                                        \"boolean\", \"null\","
            +"\n                                        {\"type\": \"record\", \"name\": \"JsonArray\","
            +"\n                                         \"fields\":["
            +"\n                                             {\"name\": \"elements\","
            +"\n                                              \"type\": {\"type\": \"array\","
            +"\n                                                       \"items\": \"JsonValue\"}}"
            +"\n                                         ]"
            +"\n                                        },"
            +"\n                                        {\"type\": \"array\", \"items\": "
            +"\n                                         {\"type\": \"record\", \"name\": \"JsonField\","
            +"\n                                          \"fields\": ["
            +"\n                                              {\"name\": \"name\", \"type\": \"string\"},"
            +"\n                                              {\"name\": \"value\", \"type\": \"JsonValue\"}"
            +"\n                                          ]"
            +"\n                                         }"
            +"\n                                        }"
            +"\n                                    ]"
            +"\n                                   }"
            +"\n                               ]"
            +"\n                              }"
            +"\n                             },"
            */
            +"\n                             {\"name\": \"order\","
            +"\n                              \"type\": {\"type\": \"enum\", \"name\": \"SortOrder\","
            +"\n                                       \"symbols\": [\"INCREASING\", \"DECREASING\","
            +"\n                                                   \"IGNORE\"]}}"
            +"\n                         ]"
            +"\n                        }"
            +"\n                       }"
            +"\n              }"
            +"\n          ]"
            +"\n         },"
            +"\n         {\"type\": \"record\", \"name\": \"Enumeration\","
            +"\n          \"fields\": ["
            +"\n              {\"name\": \"name\", \"type\": \"string\"},"
            +"\n              {\"name\": \"namespace\", \"type\": \"string\"},"
            +"\n              {\"name\": \"doc\", \"type\": \"string\"},"
            +"\n              {\"name\": \"aliases\", \"type\": {\"type\": \"array\", \"items\": {\"type\": \"string\"}}},"
            +"\n              {\"name\": \"symbols\","
            +"\n               \"type\": {\"type\": \"array\", \"items\": \"string\"}}"
            +"\n          ]"
            +"\n         },"
            +"\n         {\"type\": \"record\", \"name\": \"Fixed\","
            +"\n          \"fields\": ["
            +"\n              {\"name\": \"name\", \"type\": \"string\"},"
            +"\n              {\"name\": \"namespace\", \"type\": \"string\"},"
            +"\n              {\"name\": \"doc\", \"type\": \"string\"},"
            +"\n              {\"name\": \"aliases\", \"type\": {\"type\": \"array\", \"items\": {\"type\": \"string\"}}},"
            +"\n              {\"name\": \"size\", \"type\": \"int\"}"
            +"\n          ]"
            +"\n         },"
            +"\n         {\"type\": \"record\", \"name\": \"Array\","
            +"\n          \"fields\": ["
            +"\n              {\"name\": \"elements\", \"type\": \"Type\"}"
            +"\n          ]"
            +"\n         },"
            +"\n         {\"type\": \"record\", \"name\": \"Map\","
            +"\n          \"fields\": ["
            +"\n              {\"name\": \"values\", \"type\": \"Type\"}"
            +"\n          ]"
            +"\n         },"
            +"\n         {\"type\": \"record\", \"name\": \"Union\","
            +"\n          \"fields\": ["
            +"\n              {\"name\": \"branches\","
            +"\n               \"type\": {\"type\": \"array\", \"items\": \"Type\"}}"
            +"\n          ]"
            +"\n         },"
            +"\n         {\"type\": \"record\", \"name\": \"Primitive\","
            +"\n          \"fields\": ["
            +"\n             {\"name\": \"PrimitiveType\", \"type\": {\"type\": \"enum\", \"name\": \"PrimitiveTypes\","
            +"\n                  \"symbols\": [\"String\", \"Bytes\", \"Int\", \"Long\", \"Float\", \"Double\", \"Boolean\", \"Null\"]}}"
            +"\n          ]"
            +"\n         }"
            +"\n     ]}"
            +"\n ]"
            +"\n}";

        SCHEMA = Schema.parse(schema);
        RECORD = SCHEMA.getField("type").schema().getTypes().get(0);
    }
}
