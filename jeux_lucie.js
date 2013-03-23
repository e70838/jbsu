var NB_ROUND = 20;
var DELAY = 10;
var started=false;
var leftDigit = 0;
var rightDigit = 0;
var score = 0;
var remain = NB_ROUND;
var remainingDelay;
var timer = null;

function showMessage(delay) {
    document.getElementById("msg").innerHTML = "Score " + score + ", il reste " + remain + " multiplications." + delay; }

function displayResult(found) {
    var msg = leftDigit + " × " + rightDigit + " = " + (leftDigit * rightDigit);
    document.getElementById("operation").innerHTML = msg;
    remain--;
    showMessage("");
    if (remain > 0) {
  window.setTimeout(newChallenge, 1000);
    } else {
	alert ("Partie finie, score = " + score + "\n0 pour recommencer");
	started = false;
    }
}

function newChallenge() {
    leftDigit = Math.floor(Math.random()*11);
    rightDigit = Math.floor(Math.random()*11);
    if (leftDigit == 10 && rightDigit == 10) return newChallenge();
    document.getElementById("operation").innerHTML = leftDigit + " × " + rightDigit + " = ?";
    remainingDelay=DELAY;
    timer = window.setInterval(function(){
if (--remainingDelay >= 0) {
    showMessage(" Delai " + remainingDelay + "s");
} else {
    clearInterval(timer);timer = null;
    displayResult(false);
} }, 1000);
}

function digit(d) {
    // console.log(d);
    if (started) {
	if (timer != null) {
	    clearInterval(timer);timer = null;
        }
	if (d == leftDigit * rightDigit) {
	    score += 1 + remainingDelay;
	    displayResult(true);
	} else {
	    displayResult(false);
        }
    } else {
	if (d == 0) {
	    started=true;
	    score = 0; remain = NB_ROUND+1;
	    displayResult(true);
	} else {
	    alert ("Clique 0 pour commencer à jouer.");
	}
    }  
}
