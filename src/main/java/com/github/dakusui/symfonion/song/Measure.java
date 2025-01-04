package com.github.dakusui.symfonion.song;

/**
 * // @formatter:off
 * [source, JSON]
 * ----
 * {
 *   "$beats": "<beatsDefiningString>",
 *   "$parts": {
 *     "<partName>": {
 *         "$notes": "<strokeSequence>",
 *         "$velocityBase": "<number>",
 *         "$reverb": ["<number>", "...", "<number>"],
 *         "...": "..."
 *     }
 *   },
 *   "$groove": "<groove>",
 *   "$noteMap": {
 *      "<noteName1>": "<note>",
 *      "<noteName2>": "<note>",
 *      "<noteName3>": "<note>"
 *   }
 *   "$labels": ["<label1>", "<label2>", "..."]
 * }
 * ----
 * // @formatter:on
 */
public class Measure {
  public Measure() {

  }
}
