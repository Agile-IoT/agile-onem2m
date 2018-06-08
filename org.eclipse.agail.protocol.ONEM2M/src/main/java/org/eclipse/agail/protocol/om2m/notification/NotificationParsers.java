package org.eclipse.agail.protocol.om2m.notification;

import org.apache.commons.io.IOUtils;

public class NotificationParsers {

    private static final NotificationParser[] PARSERS = new NotificationParser[] {
            new NotificationParserJson(),
            new NotificationParserXml()
    };

    public static Notification parse(String text, String mediaType) {
        for (NotificationParser parser : PARSERS) {
            if (parser.support(mediaType)) {
                return parser.parse(IOUtils.toInputStream(text));
            }
        }
        throw new IllegalArgumentException("Cannot parse notification for unknown media type " + mediaType);
    }
}
