
$(document).ready(function() {

    $('.ui-slider-handle').live('touchstart', function(){
        // When user touches the slider handle, temporarily unbind the page turn handlers
        doUnbind();
    });

    $('.ui-slider-handle').live('mousedown', function(){
        // When user touches the slider handle, temporarily unbind the page turn handlers
        doUnbind();
    });

    $('.ui-slider-handle').live('touchend', function(){
        //When the user let's go of the handle, rebind the controls for page turn
        // Put in a slight delay so that the rebind does not happen until after the swipe has been triggered
        setTimeout( function() {doBind();}, 100 );
    });

    $('.ui-slider-handle').live('mouseup', function(){
        //When the user let's go of the handle, rebind the controls for page turn
        // Put in a slight delay so that the rebind does not happen until after the swipe has been triggered
        setTimeout( function() {doBind();}, 100 );
    });

    // Set the initial window (assuming it will always be #1
    window.now = 0;

    //get an Array of all of the pages and count
    windowMax = $('div[data-role="page"]').length; 

   doBind();
});
    // Functions for binding swipe events to named handlers
    function doBind() {
        $('div[data-role="page"]').live("swipeleft", turnPage); 
        $('div[data-role="page"]').live("swiperight", turnPageBack);
    }

    function doUnbind() {
        $('div[data-role="page"]').die("swipeleft", turnPage);
        $('div[data-role="page"]').die("swiperight", turnPageBack);
    }

    // Named handlers for binding page turn controls
    function turnPage(){
        // Check to see if we are already at the highest numbers page            
        if (window.now < windowMax) {
            window.now++
            $.mobile.changePage("#page"+window.now, "slide", false, true);
        }
    }

    function turnPageBack(){
        // Check to see if we are already at the lowest numbered page
        if (window.now != 1) {
            window.now--;
            $.mobile.changePage("#page"+window.now, "slide", true, true);
        }
    }
    
    function page (pageIdx)
    {
    window.now = pageIdx;
    	  $.mobile.changePage("#page"+pageIdx, "slide", false, true);
    }
    