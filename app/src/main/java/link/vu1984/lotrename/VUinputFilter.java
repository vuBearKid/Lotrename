package link.vu1984.lotrename;

import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;

import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/6/5.
 */
public class VUinputFilter {
    private VUinputFilter() {
    }

    public static class TestFilter implements InputFilter {
        @Override
        /**
         * @param source 输入的字符
         * @param start 输入字符在source开始位置
         * @param end 输入字符在source结束位置
         * @param dest 原有字符
         * @param dstart 新字符在原字符的开始位置，当是删除时，代表删除的位置
         * @param dend 新字符在原字符的结束位置
         * @return 为新输出的字符
         */
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            VULog.e("NoNewLinerFilter", "sourceClass:" + source.getClass().getSimpleName() + " source:" + source + " start:" + start + " end:" + end + " dest:" + dest + " dstart:" + dstart + " dend:" + dend);
            return null;
        }
    }

    /**
     * 替换符合对应该正则的字符
     */
    public static class RegexFilter implements InputFilter {
        private String regex =".";
        /**
         * 构造函数
         * @param pattern Pattern.compile(regex)  pattern为regex
         */
        public RegexFilter(String pattern){//一般是一个字符一个字符地输入的，但也有可以是一串字符这样的输入，如拷贝黏贴或SPANNED（为一种显示方式，如HTML里的SPAN）
            regex = pattern;
        }
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            //VULog.e("NoNewLinerFilter","sourceClass:"+source.getClass().getSimpleName()+" source:"+source+" start:"+start+" end:"+end+" dest:"+dest+" dstart:"+dstart+" dend:"+dend);
            if (start == 0 && end == 0) return null;//删除操作，直接返回
            Pattern p = Pattern.compile(regex);//VULog.e("NoNewLinerFilter",p.pattern());
            char[] s = new char[end - start];
            TextUtils.getChars(source, start, end, s, 0);
            String str = new String(s);
            str = p.matcher(str).replaceAll("");

            if (source instanceof Spanned) {//这个很重要，在英文输入时，有些输入法为span模式，要求返回SpannableString，不然作为一般string反回的话会累加
                SpannableString strp = new SpannableString(str);
                TextUtils.copySpansFrom((Spanned) source, start, end, null, strp, 0);
                return strp;
            } else {
                return str;
            }
        }
    }

    /**
     * 不允许换行
     */
    public static class NoNewLinerFilter implements InputFilter {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            //VULog.e("NoNewLinerFilter","sourceClass:"+source.getClass().getSimpleName()+" source:"+source+" start:"+start+" end:"+end+" dest:"+dest+" dstart:"+dstart+" dend:"+dend);
            if (start == 0 && end == 0) return null;//删除操作，直接返回

            Pattern p = Pattern.compile("(\\n|\\n\\r|\\r\\n|\\r)");//VULog.e("NoNewLinerFilter",p.pattern());
            char[] s = new char[end - start];
            TextUtils.getChars(source, start, end, s, 0);
            String str = new String(s);
            str = str.replaceAll(p.pattern(), "");

            if (source instanceof Spanned) {//这个很重要，在英文输入时，有些输入法为span模式，要求返回SpannableString，不然作为一般string反回的话会累加
                SpannableString strp = new SpannableString(str);
                TextUtils.copySpansFrom((Spanned) source, start, end, null, strp, 0);
                return strp;
            } else {
                return str;
            }
        }
    }

    /**
     * copy from InputFilter.AllCaps
     */
    public static class AllCaps implements InputFilter {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            VULog.e("NoNewLinerFilter", "sourceClass:" + source.getClass().getSimpleName() + " source:" + source + " start:" + start + " end:" + end + " dest:" + dest + " dstart:" + dstart + " dend:" + dend);
            for (int i = start; i < end; i++) {
                VULog.e("NoNewLinerFilter", i + "");
                if (Character.isLowerCase(source.charAt(i))) {
                    char[] v = new char[end - start];
                    TextUtils.getChars(source, start, end, v, 0);
                    String s = new String(v).toUpperCase();

                    if (source instanceof Spanned) {
                        SpannableString sp = new SpannableString(s);
                        TextUtils.copySpansFrom((Spanned) source,
                                start, end, null, sp, 0);
                        VULog.e("NoNewLinerFilter",  "sp:" + sp);
                        return sp;
                    } else {
                        VULog.e("NoNewLinerFilter",  "s:" + s);
                        return s;
                    }
                }
            }

            return null; // keep original
        }
    }
    //=================================================================================================================
    public static class FileExtensionFilter implements InputFilter {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            //VULog.e("NoNewLinerFilter","sourceClass:"+source.getClass().getSimpleName()+" source:"+source+" start:"+start+" end:"+end+" dest:"+dest+" dstart:"+dstart+" dend:"+dend);
            if (start == 0 && end == 0) return null;//删除操作，直接返回
            Pattern p = Pattern.compile("[^a-zA-Z0-9]+");
            char[] s = new char[end - start];
            TextUtils.getChars(source, start, end, s, 0);
            String str = new String(s);
            str = str.replaceAll(p.pattern(), "");
            if (source instanceof Spanned) {//这个很重要，在英文输入时，有些输入法为span模式，要求返回SpannableString，不然作为一般string反回的话会累加
                SpannableString strp = new SpannableString(str);
                TextUtils.copySpansFrom((Spanned) source, start, end, null, strp, 0);
                //VULog.e("NoNewLinerFilter","strp:"+strp);
                return strp;
            } else {
                //VULog.e("NoNewLinerFilter","str:"+str);
                return str;
            }

        }
    }

    public static class FilePrefixFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (start == 0 && end == 0) return null;//删除操作，直接返回
            Pattern p = Pattern.compile("[\\\\\\?/:*|]+");//因为<self>，只能保留<>
            char[] s = new char[end - start];
            TextUtils.getChars(source, start, end, s, 0);
            String str = new String(s);
            str = str.replaceAll(p.pattern(), "");
            if (source instanceof Spanned) {//这个很重要，在英文输入时，有些输入法为span模式，要求返回SpannableString，不然作为一般string反回的话会累加
                SpannableString strp = new SpannableString(str);
                TextUtils.copySpansFrom((Spanned) source, start, end, null, strp, 0);
                //VULog.e("NoNewLinerFilter","strp:"+strp);
                return strp;
            } else {
                //VULog.e("NoNewLinerFilter","str:"+str);
                return str;
            }
        }
    }


}
