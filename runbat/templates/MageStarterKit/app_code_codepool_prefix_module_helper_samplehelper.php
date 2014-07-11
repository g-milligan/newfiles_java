<<filename:c:app/code/[codepool]/[Prefix]/[Module]/Helper:"SampleHelper">>
<<var:n:module prefix => [Prefix]>>
<<var:c:module name => [Module]>>
<<var:l:code pool => [codepool]>>

<?php
//helpers will extend Mage_Core_Helper_Abstract 
class [Prefix]_[Module]_Helper_SampleHelper extends Mage_Core_Helper_Abstract{
	function shouldSayHi() {
		return true;
    }
}
?>