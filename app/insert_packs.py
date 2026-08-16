import re

new_packs = """        ,
        GenPack(
            id = "cj_disney_quiz",
            title = "Prinzessinnen, Piraten und Feenstaub: Disney-Quiz",
            cat = "nie",
            topic = "filme_serien",
            type = "quiz",
            tags = listOf("disney", "ichhabenochnie"),
            questions = listOf(
                GenQuestion(q = "Ich habe noch nie bei einem Disney-Film geweint."),
                GenQuestion(q = "Ich habe noch nie alle Lieder aus \\"Der König der Löwen\\" auswendig gekonnt."),
                GenQuestion(q = "Ich habe noch nie heimlich gehofft, dass mein Haustier sprechen kann wie bei Disney."),
                GenQuestion(q = "Ich habe noch nie einen Disney-Film dreimal hintereinander geschaut."),
                GenQuestion(q = "Ich habe noch nie einen Bösewicht cooler als den Helden gefunden."),
                GenQuestion(q = "Ich habe noch nie versucht, mit Tieren zu singen wie Schneewittchen."),
                GenQuestion(q = "Ich habe noch nie von einem Ausflug ins Disneyland geträumt."),
                GenQuestion(q = "Ich habe noch nie einen Disney-Ohrwurm für eine ganze Woche gehabt."),
                GenQuestion(q = "Ich habe noch nie behauptet, ich sei zu alt für Disney-Filme (und es heimlich genossen)."),
                GenQuestion(q = "Ich habe noch nie ein Zitat von Meister Yoda aus Star Wars im Alltag verwendet."),
                GenQuestion(q = "Ich habe noch nie einen Marvel-Film nur wegen der Post-Credit-Scene bis zum Ende geschaut."),
                GenQuestion(q = "Ich habe noch nie bei \\"Oben\\" (Up) schon in den ersten 10 Minuten Tränen in den Augen gehabt."),
                GenQuestion(q = "Ich habe noch nie einen Charakter aus Toy Story in meinem eigenen Spielzeug wiedererkannt."),
                GenQuestion(q = "Ich habe noch nie so getan, als hätte ich Zauberkräfte wie Elsa."),
                GenQuestion(q = "Ich habe noch nie ein Disney-Lied unter der Dusche geschmettert.")
            )
        ),
        GenPack(
            id = "cj_entertainment_quiz",
            title = "Die ultimative Entertainment-Quiz-Herausforderung",
            cat = "nie",
            topic = "filme_serien",
            type = "quiz",
            tags = listOf("entertainment", "ichhabenochnie"),
            questions = listOf(
                GenQuestion(q = "Ich habe noch nie eine ganze Serienstaffel an einem Tag durchgeschaut (Binge-Watching)."),
                GenQuestion(q = "Ich habe noch nie das Ende eines Films gegoogelt, weil ich nicht abwarten konnte."),
                GenQuestion(q = "Ich habe noch nie einen Film-Spoiler verraten und es sofort bereut."),
                GenQuestion(q = "Ich habe noch nie für meinen Lieblingscharakter in einer Serie geschwärmt."),
                GenQuestion(q = "Ich habe noch nie einen Film nur wegen eines bestimmten Schauspielers geschaut."),
                GenQuestion(q = "Ich habe noch nie das Intro meiner Lieblingsserie übersprungen."),
                GenQuestion(q = "Ich habe noch nie bei einem Horrorfilm die Augen zugehalten."),
                GenQuestion(q = "Ich habe noch nie das Netflix-Passwort von jemand anderem mitbenutzt."),
                GenQuestion(q = "Ich habe noch nie so laut gelacht, dass ich eine wichtige Film-Szene verpasst habe."),
                GenQuestion(q = "Ich habe noch nie eine Serie abgebrochen, weil die letzte Staffel so schlecht war."),
                GenQuestion(q = "Ich habe noch nie einen Film-Soundtrack stundenlang auf Repeat gehört."),
                GenQuestion(q = "Ich habe noch nie eine Film-Empfehlung gegeben, die der andere furchtbar fand."),
                GenQuestion(q = "Ich habe noch nie eine Oscar-Verleihung live mitten in der Nacht verfolgt."),
                GenQuestion(q = "Ich habe noch nie bei einer peinlichen Szene den Raum verlassen (Cringe-Faktor)."),
                GenQuestion(q = "Ich habe noch nie ein Popcorn im Kino fallen gelassen und es trotzdem gegessen.")
            )
        ),
        GenPack(
            id = "cj_hogwarts_quiz",
            title = "Hogwarts Haus Stolz: Wo gehörst du hin?",
            cat = "nie",
            topic = "filme_serien",
            type = "quiz",
            tags = listOf("harrypotter", "ichhabenochnie"),
            questions = listOf(
                GenQuestion(q = "Ich habe noch nie auf meinen Brief aus Hogwarts gewartet."),
                GenQuestion(q = "Ich habe noch nie einen Online-Test gemacht, um mein Hogwarts-Haus herauszufinden."),
                GenQuestion(q = "Ich habe noch nie versucht, einen Zauberspruch mit einem Stock auszuprobieren."),
                GenQuestion(q = "Ich habe noch nie ein Butterbier nachgemacht oder getrunken."),
                GenQuestion(q = "Ich habe noch nie \\"Expecto Patronum\\" in einer gruseligen Situation geflüstert."),
                GenQuestion(q = "Ich habe noch nie mit einem Freund gestritten, welches Haus das beste ist."),
                GenQuestion(q = "Ich habe noch nie alle Harry-Potter-Filme an einem Wochenende durchgeschaut."),
                GenQuestion(q = "Ich habe noch nie einen der dicken Harry-Potter-Bände an einem Tag gelesen."),
                GenQuestion(q = "Ich habe noch nie geträumt, Quidditch zu spielen."),
                GenQuestion(q = "Ich habe noch nie jemanden als \\"Muggel\\" bezeichnet, weil er etwas nicht verstanden hat."),
                GenQuestion(q = "Ich habe noch nie einen Harry-Potter-Film-Marathon an Weihnachten gemacht."),
                GenQuestion(q = "Ich habe noch nie heimlich gehofft, einen Zeitumkehrer zu besitzen."),
                GenQuestion(q = "Ich habe noch nie die Karte des Rumtreibers zitiert."),
                GenQuestion(q = "Ich habe noch nie ein Haustier nach einem Charakter aus Hogwarts benannt."),
                GenQuestion(q = "Ich habe noch nie behauptet, Slytherin sei eigentlich gar nicht so böse.")
            )
        ),
        GenPack(
            id = "cj_videogame_quiz",
            title = "Bist du ein Videospiel-Guru?",
            cat = "nie",
            topic = "hobbys",
            type = "quiz",
            tags = listOf("games", "ichhabenochnie"),
            questions = listOf(
                GenQuestion(q = "Ich habe noch nie die Schuld auf den Controller geschoben, wenn ich verloren habe."),
                GenQuestion(q = "Ich habe noch nie bis spät in die Nacht gezockt und am nächsten Tag bereut, wie müde ich war."),
                GenQuestion(q = "Ich habe noch nie \\"Nur noch eine Runde\\" gesagt und dann noch drei Stunden gespielt."),
                GenQuestion(q = "Ich habe noch nie bei einem Spiel vergessen zu speichern und stundenlangen Fortschritt verloren."),
                GenQuestion(q = "Ich habe noch nie einen Wutanfall bekommen (Rage Quit) und das Spiel beendet."),
                GenQuestion(q = "Ich habe noch nie heimlich gegoogelt, wie man ein schweres Level löst."),
                GenQuestion(q = "Ich habe noch nie echtes Geld für kosmetische Items in einem Spiel ausgegeben."),
                GenQuestion(q = "Ich habe noch nie einen Boss beim ersten Versuch besiegt und mich wie ein Gott gefühlt."),
                GenQuestion(q = "Ich habe noch nie bei Mario Kart absichtlich jemanden mit einem blauen Panzer abgeschossen."),
                GenQuestion(q = "Ich habe noch nie in Sims den Pool-Leiter entfernt."),
                GenQuestion(q = "Ich habe noch nie einen NPC (Non-Player Character) absichtlich geärgert."),
                GenQuestion(q = "Ich habe noch nie die Tastatur oder den Controller vor Wut weggeworfen."),
                GenQuestion(q = "Ich habe noch nie einen Charakter in einem RPG genau nach meinem Aussehen erstellt."),
                GenQuestion(q = "Ich habe noch nie vor einem Jump-Scare im Spiel laut geschrien."),
                GenQuestion(q = "Ich habe noch nie den Soundtrack eines Spiels beim Arbeiten oder Lernen gehört.")
            )
        ),
        GenPack(
            id = "cj_universal_quiz",
            title = "Universal Studios Genie oder Neuling?",
            cat = "nie",
            topic = "filme_serien",
            type = "quiz",
            tags = listOf("universal", "parks", "ichhabenochnie"),
            questions = listOf(
                GenQuestion(q = "Ich habe noch nie von einer Fahrt in der Jurassic Park Wasserbahn geträumt."),
                GenQuestion(q = "Ich habe noch nie einen Themenpark besucht und von morgens bis abends durchgehalten."),
                GenQuestion(q = "Ich habe noch nie Angst vor einer Achterbahn gehabt und bin trotzdem eingestiegen."),
                GenQuestion(q = "Ich habe noch nie ein überteuertes Souvenir im Freizeitpark gekauft."),
                GenQuestion(q = "Ich habe noch nie ein Foto mit einem Park-Maskottchen gemacht."),
                GenQuestion(q = "Ich habe noch nie mehr als zwei Stunden für eine einzige Attraktion angestanden."),
                GenQuestion(q = "Ich habe noch nie beim Achterbahn-Foto eine lustige Pose gemacht."),
                GenQuestion(q = "Ich habe noch nie Fast-Pass-Tickets gekauft, weil ich ungeduldig war."),
                GenQuestion(q = "Ich habe noch nie in einem 3D-Simulator-Ride die Orientierung verloren."),
                GenQuestion(q = "Ich habe noch nie einen Transformers- oder Marvel-Ride völlig fasziniert verlassen."),
                GenQuestion(q = "Ich habe noch nie einen Churro in einem Themenpark gegessen."),
                GenQuestion(q = "Ich habe noch nie das Gefühl gehabt, in einem Filmset zu stehen."),
                GenQuestion(q = "Ich habe noch nie so viel Adrenalin gespürt, dass ich direkt nochmal fahren wollte."),
                GenQuestion(q = "Ich habe noch nie meine Wertsachen bei einer Wasserbahn nass gemacht."),
                GenQuestion(q = "Ich habe noch nie einen Tag im Themenpark als den besten Tag des Jahres bezeichnet.")
            )
        ),
        GenPack(
            id = "cj_party_wer1",
            title = "Party Edition",
            cat = "wer",
            topic = "aufwaermen",
            type = "quiz",
            tags = listOf("party", "werwuerde"),
            questions = listOf(
                GenQuestion(q = "Wer würde eher auf einer Party als Erster einschlafen?"),
                GenQuestion(q = "Wer würde eher den Text eines Songs falsch mitsingen, aber voller Überzeugung?"),
                GenQuestion(q = "Wer würde eher versehentlich das Getränk von jemand anderem trinken?"),
                GenQuestion(q = "Wer würde eher eine peinliche Tanz-Performance hinlegen und es feiern?"),
                GenQuestion(q = "Wer würde eher am nächsten Morgen seinen eigenen Namen vergessen?"),
                GenQuestion(q = "Wer würde eher heimlich die Party verlassen, ohne sich zu verabschieden?"),
                GenQuestion(q = "Wer würde eher den DJ nerven, um den eigenen Lieblingssong zu wünschen?"),
                GenQuestion(q = "Wer würde eher auf einer Party plötzlich anfangen aufzuräumen?"),
                GenQuestion(q = "Wer würde eher stundenlang tiefgründige Gespräche in der Küche führen?"),
                GenQuestion(q = "Wer würde eher bei einem Trinkspiel betrügen?"),
                GenQuestion(q = "Wer würde eher versehentlich eine teure Vase oder etwas Zerbrechliches umwerfen?"),
                GenQuestion(q = "Wer würde eher die Nachbarn einladen, wenn sie sich über den Lärm beschweren?"),
                GenQuestion(q = "Wer würde eher auf dem Sofa eines Fremden übernachten?"),
                GenQuestion(q = "Wer würde eher auf einer Party Pizza für alle bestellen und sie selbst aufessen?"),
                GenQuestion(q = "Wer würde eher mit dem Haustier des Gastgebers spielen, anstatt mit den Gästen zu reden?"),
                GenQuestion(q = "Wer würde eher das peinlichste Foto des Abends in seiner Story posten?")
            )
        ),
        GenPack(
            id = "cj_party_wer2",
            title = "Party Edition II",
            cat = "wer",
            topic = "aufwaermen",
            type = "quiz",
            tags = listOf("party", "werwuerde"),
            questions = listOf(
                GenQuestion(q = "Wer würde eher eine Flasche öffnen, ohne einen Flaschenöffner zu benutzen?"),
                GenQuestion(q = "Wer würde eher eine Rede halten, obwohl absolut niemand danach gefragt hat?"),
                GenQuestion(q = "Wer würde eher auf der Tanzfläche ausrutschen, es aber wie einen coolen Move aussehen lassen?"),
                GenQuestion(q = "Wer würde eher den teuersten Drink auf der Karte bestellen und es bereuen?"),
                GenQuestion(q = "Wer würde eher einen Ex-Partner anrufen, nachdem die Party vorbei ist?"),
                GenQuestion(q = "Wer würde eher die Türsteher anfreunden, um VIP-Zugang zu bekommen?"),
                GenQuestion(q = "Wer würde eher ein spontanes Karaoke-Battle starten?"),
                GenQuestion(q = "Wer würde eher am nächsten Tag ein Video finden, von dem er nichts mehr wusste?"),
                GenQuestion(q = "Wer würde eher das Buffet plündern und heimlich Essen mit nach Hause nehmen?"),
                GenQuestion(q = "Wer würde eher einen fremden Gast versehentlich beleidigen?"),
                GenQuestion(q = "Wer würde eher auf der Party einschlafen und mit Edding im Gesicht aufwachen?"),
                GenQuestion(q = "Wer würde eher versuchen, einen komplizierten Cocktail selbst zu mixen und kläglich scheitern?"),
                GenQuestion(q = "Wer würde eher die Party-Spiele (wie Beer Pong) viel zu ernst nehmen?"),
                GenQuestion(q = "Wer würde eher eine Stunde im Badezimmer verbringen, um Selfies zu machen?"),
                GenQuestion(q = "Wer würde eher auf einer Hausparty die Kontrolle über die Spotify-Playlist an sich reißen?"),
                GenQuestion(q = "Wer würde eher behaupten, komplett nüchtern zu sein, während das Gegenteil der Fall ist?")
            )
        )
"""

with open("app/src/main/java/com/example/data/GeneratedHarmonyContent.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Find the closing brace of PACKS
# Since PACKS is just a list of GenPack, we find `    )` before `    val LINK_PACKS`
replacement = new_packs + "\\n    )"
content = content.replace("    )\\n    val LINK_PACKS", replacement + "\\n    val LINK_PACKS")

with open("app/src/main/java/com/example/data/GeneratedHarmonyContent.kt", "w", encoding="utf-8") as f:
    f.write(content)

print("Done")
