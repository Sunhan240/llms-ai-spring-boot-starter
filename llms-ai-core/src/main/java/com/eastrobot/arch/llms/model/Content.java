package com.eastrobot.arch.llms.model;


import java.util.Map;

/**
 * Data structure that contains content and metadata. Common parent for the
 *
 * @author Mark Pollack
 * @author Christian Tzolov
 * @since 1.0.0
 */
public interface Content {

    /**
     * Get the content of the message.
     *
     * @return content
     */
    String getContent();

    /**
     * return Get the metadata associated with the content.
     *
     * @return metadata
     */
    Map<String, Object> getMetadata();

}
