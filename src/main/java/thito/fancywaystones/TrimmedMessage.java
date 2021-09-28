package thito.fancywaystones;

import org.bukkit.*;

import java.util.*;

public class TrimmedMessage {

    interface Char {
        String toString();
    }

    static class ColorChar implements Char {
        String string;

        public ColorChar(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    static ColorChar defaultColorChar = new ColorChar(ChatColor.WHITE.toString());
    static class NormalChar implements Char {

        ColorChar colorChar = defaultColorChar;
        char c;

        public NormalChar(char c) {
            this.c = c;
        }

        @Override
        public String toString() {
            return colorChar.toString() + c;
        }
    }

    List<NormalChar> chars;

    public TrimmedMessage(String message) {
        chars = new ArrayList<>(message.length());
        Char last = null;
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c == ChatColor.COLOR_CHAR) {
                if (i + 1 < message.length()) {
                    char color = message.charAt(i + 1);
                    if ((color == 'x' || color == 'X') && (i + 6 < message.length())) {
                        String hex = message.substring(i, i + 6);
                        if (last instanceof ColorChar) {
                            ((ColorChar) last).string = ChatColor.COLOR_CHAR + "x" + hex;
                        } else {
                            last = new ColorChar(ChatColor.COLOR_CHAR + "x" + hex);
                        }
                        i += 6;
                    } else {
                        ChatColor col = ChatColor.getByChar(color);
                        if (col != null) {
                            i++;
                            if (col.isColor() || col == ChatColor.RESET) {
                                if (last instanceof ColorChar) {
                                    ((ColorChar) last).string = col == ChatColor.RESET ? ChatColor.WHITE.toString() : col.toString();
                                } else {
                                    last = new ColorChar(col.toString());
                                }
                            } else {
                                if (last instanceof ColorChar) {
                                    ((ColorChar) last).string += col.toString();
                                } else {
                                    last = new ColorChar(col.toString());
                                }
                            }
                            continue;
                        }
                    }
                }
            }

            NormalChar nc = new NormalChar(c);
            if (last instanceof ColorChar) {
                nc.colorChar = (ColorChar) last;
            } else if (last instanceof NormalChar) {
                nc.colorChar = ((NormalChar) last).colorChar;
            }
            chars.add(nc);
            last = nc;
        }
    }

    public int getMaxRadius() {
        return chars.size() % 2 == 0 ? chars.size() / 2 + 1 : chars.size() / 2 + 2;
    }

    public String trimFromCenter(int radius) {
        StringBuilder builder = new StringBuilder(radius * 2);
        int leftStart, rightStart;
        if (chars.size() % 2 != 0) {
            int center = chars.size() / 2 + 1;
            leftStart = center - 1;
            rightStart = center + 1;
            NormalChar c = chars.get(center);
            int previousIndex = center - 1;
            Object result;
            if (previousIndex >= 0 && previousIndex < chars.size() && radius > 0) {
                NormalChar previous = chars.get(previousIndex);
                if (previous.colorChar.toString().equals(c.colorChar.toString())) {
                    result = c.c;
                } else {
                    result = c.toString();
                }
            } else {
                result = c.toString();
            }
            builder.append(result);
        } else {
            leftStart = chars.size() / 2 - 1;
            rightStart = chars.size() / 2;
        }
        for (int target = 0; target < radius; target++) {
            if (leftStart - target >= 0 && leftStart - target < chars.size()) {
                NormalChar left = chars.get(leftStart - target);
                int previousIndex = leftStart - (target + 1);
                Object result;
                if (previousIndex >= 0 && previousIndex < chars.size() && target + 1 >= 0 && target + 1 < radius) {
                    NormalChar previousChar = chars.get(previousIndex);
                    if (previousChar.colorChar.toString().equals(left.colorChar.toString())) {
                        result = left.c;
                    } else {
                        result = left.toString();
                    }
                } else {
                    result = left.toString();
                }
                builder.insert(0, result);
            }
            if (rightStart + target >= 0 && rightStart + target < chars.size()) {
                NormalChar right = chars.get(rightStart + target);
                int previousIndex = rightStart + (target - 1);
                Object result;
                if (previousIndex >= 0 && previousIndex < chars.size() && target - 1 >= 0 && target - 1 < radius) {
                    NormalChar previousChar = chars.get(previousIndex);
                    if (previousChar.colorChar.toString().equals(right.colorChar.toString())) {
                        result = right.c;
                    } else {
                        result = right.toString();
                    }
                } else {
                    result = right.toString();
                }
                builder.append(result);
            }
        }
        return builder.toString();
    }

    private ColorChar findColorChar(int index) {
        for (; index >= 0 && index < chars.size(); index--) {
            Char c = chars.get(index);
            if (c instanceof ColorChar) {
                return (ColorChar) c;
            }
        }
        return null;
    }

    public String shiftLeft(int amount) {
        StringBuilder builder = new StringBuilder(chars.size());
        for (int i = 0; i < chars.size(); i++) {
            NormalChar current = chars.get((i + amount) % chars.size());
            Object result;
            int previousIndex = ((i - 1) + amount) % chars.size();
            if (previousIndex >= 0 && previousIndex < chars.size() && i > 0) {
                NormalChar previous = chars.get(previousIndex);
                if (previous.colorChar.toString().equals(current.colorChar.toString())) {
                    result = current.c;
                } else {
                    result = current.toString();
                }
            } else {
                result = current.toString();
            }
            builder.append(result);
        }
        return builder.toString();
    }
}
