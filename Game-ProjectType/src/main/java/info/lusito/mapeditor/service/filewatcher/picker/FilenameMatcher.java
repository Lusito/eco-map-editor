package info.lusito.mapeditor.service.filewatcher.picker;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilenameMatcher {
    private final Pattern regex;
    private final ArrayList<Boolean> highlightGroups = new ArrayList();
    
    public FilenameMatcher(String pattern, boolean caseSensitive) {
        
        boolean exactMatch = false;
        boolean lastWasChar = false;
		StringBuilder pb = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            switch(ch) {
                case '?':
                    highlightGroups.add(false);
                    pb.append("(.)");
                    break;
                case '*':
                    highlightGroups.add(false);
                    pb.append("(.*)");
                    break;
                case ' ':
                    if(i == pattern.length()-1) {
                        exactMatch = true;
                        break;
                    }
                default:
                    // Add implicit *
                    if(lastWasChar) {
                        if(Character.isUpperCase(ch)) {
                            highlightGroups.add(false);
                            pb.append("(.*)");
                        }
                    }
                    highlightGroups.add(true);
                    pb.append("(").append(Pattern.quote(ch + "")).append(")");
                    lastWasChar = true;
                    break;
            }
        }
        if(!exactMatch) {
            highlightGroups.add(false);
            pb.append("(.*)");
        }
        regex = Pattern.compile(pb.toString(), caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
    }
    
    public String match(String input) {
		Matcher regexMatcher = regex.matcher(input);
		if(regexMatcher.matches()) {
            int i=1;
            boolean inHighlight = false;
            StringBuilder resultString = new StringBuilder();
            for (Boolean highlighted : highlightGroups) {
                if(highlighted != inHighlight) {
                    inHighlight = highlighted;
                    resultString.append(highlighted ? "<b>" : "</b>");
                }
                resultString.append(regexMatcher.group(i++));
            }
            if(inHighlight)
                resultString.append("</b>");
            return resultString.toString();
		}
        return null;
    }
}
