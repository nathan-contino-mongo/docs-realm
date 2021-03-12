package com.mongodb.realm.examples.model.kotlin
// :code-block-start: complete
// :replace-start: {
//    "terms": {
//       "HauntedHouseKt": "HauntedHouse"
//    }
// }
import io.realm.MutableRealmInteger;
import io.realm.RealmObject;
import io.realm.annotations.Required

open class HauntedHouseKt: RealmObject() {
    @Required
    val ghosts: MutableRealmInteger = MutableRealmInteger.valueOf(0)
}
// :replace-end:
// :code-block-end: