/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bunkr.core.inventory;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created At: 2015-12-13
 */
public class FolderInventoryItemJSON
{
    @SuppressWarnings("unchecked")
    public static JSONAware encodeO(FolderInventoryItem input)
    {
        JSONObject out = new JSONObject();

        out.put("name", input.getName());
        out.put("uuid", input.getUuid().toString());

        JSONArray files = input.getFiles().stream()
                .map(FileInventoryItemJSON::encodeO)
                .collect(Collectors.toCollection(JSONArray::new));
        out.put("files", files);

        JSONArray folders = input.getFolders().stream()
                .map(FolderInventoryItemJSON::encodeO)
                .collect(Collectors.toCollection(JSONArray::new));
        out.put("folders", folders);
        return out;
    }

    public static String encode(FolderInventoryItem input)
    {
        return encodeO(input).toJSONString();
    }

    @SuppressWarnings("unchecked")
    public static FolderInventoryItem decodeO(JSONObject input)
    {
        FolderInventoryItem outputFolder = new FolderInventoryItem(
                (String) input.get("name"),
                UUID.fromString((String) input.get("uuid")),
                new ArrayList<>(),
                new ArrayList<>()
        );

        for (Object item : (JSONArray) input.get("files"))
        {
            outputFolder.addFile(FileInventoryItemJSON.decodeO((JSONObject) item));
        }

        for (Object item : (JSONArray) input.get("folders"))
        {
            outputFolder.addFolder(FolderInventoryItemJSON.decodeO((JSONObject) item));
        }

        return outputFolder;
    }

    public static FolderInventoryItem decode(String input)
    {
        return decodeO((JSONObject) JSONValue.parse(input));
    }
}
