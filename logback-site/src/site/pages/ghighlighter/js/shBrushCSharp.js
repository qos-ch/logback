/*
 * JsMin
 * Javascript Compressor
 * http://www.crockford.com/
 * http://www.smallsharptools.com/
*/

dp.sh.Brushes.CSharp=function()
{var keywords='abstract as base bool break byte case catch char checked class const '+'continue decimal default delegate do double else enum event explicit '+'extern false finally fixed float for foreach get goto if implicit in int '+'interface internal is lock long namespace new null object operator out '+'override params private protected public readonly ref return sbyte sealed set '+'short sizeof stackalloc static string struct switch this throw true try '+'typeof uint ulong unchecked unsafe ushort using virtual void while';this.regexList=[{regex:dp.sh.RegexLib.SingleLineCComments,css:'comment'},{regex:dp.sh.RegexLib.MultiLineCComments,css:'comment'},{regex:dp.sh.RegexLib.DoubleQuotedString,css:'string'},{regex:dp.sh.RegexLib.SingleQuotedString,css:'string'},{regex:new RegExp('^\\s*#.*','gm'),css:'preprocessor'},{regex:new RegExp(this.GetKeywords(keywords),'gm'),css:'keyword'}];this.CssClass='dp-c';this.Style='.dp-c .vars { color: #d00; }';}
dp.sh.Brushes.CSharp.prototype=new dp.sh.Highlighter();dp.sh.Brushes.CSharp.Aliases=['c#','c-sharp','csharp'];
