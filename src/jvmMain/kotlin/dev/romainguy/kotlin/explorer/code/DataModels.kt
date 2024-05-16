/*
 * Copyright (C) 2024 Romain Guy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.romainguy.kotlin.explorer.code

import androidx.collection.ScatterSet
import androidx.collection.scatterSetOf

enum class ISA(val branchInstructions: ScatterSet<String>) {
    ByteCode(scatterSetOf("if")),
    Dex(scatterSetOf("if")),
    X86_64(
        scatterSetOf(
            "je",
            "jz",
            "jne",
            "jnz",
            "js",
            "jns",
            "jg",
            "jnle",
            "jge",
            "jnl",
            "jl",
            "jnge",
            "jle",
            "jng",
            "ja",
            "jnbe",
            "jae",
            "jnb",
            "jb",
            "jnae",
            "jbe",
            "jna"
        )
    ),
    Arm64(scatterSetOf("b", "bl", "cbz", "cbnz", "tbz", "tbnz"))
}

data class Class(val header: String, val methods: List<Method>)

data class Method(val header: String, val instructionSet: InstructionSet)

data class InstructionSet(val instructions: List<Instruction>, val isa: ISA)

data class Instruction(val address: Int, val code: String, val jumpAddress: Int?, val lineNumber: Int? = null)

fun List<Instruction>.withLineNumbers(lineNumbers: Map<Int, Int>): List<Instruction> {
    return map { it.copy(lineNumber = lineNumbers[it.address]) }
}
