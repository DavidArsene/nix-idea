package org.nixos.idea.lang

import com.intellij.lexer.FlexAdapter

class NixLexer : FlexAdapter(object : _NixLexer(null) {
    // todo: Implement RestartableLexer when it becomes non-experimental. See
    // https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010305800/comments/360002861979

    override fun reset(buffer: CharSequence?, start: Int, end: Int, initialState: Int) {
        onReset()
        super.reset(buffer, start, end, initialState)
    }
})
