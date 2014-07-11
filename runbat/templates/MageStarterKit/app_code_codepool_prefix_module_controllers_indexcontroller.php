<<filename:c:app/code/[codepool]/[Prefix]/[Module]/controllers:"IndexController">>
<<var:n:module prefix => [Prefix]>>
<<var:c:module name => [Module]>>
<<var:l:module prefix => [prefix]>>
<<var:l:module name => [module]>>
<<var:l:code pool => [codepool]>>

<?php
class [Prefix]_[Module]_IndexController extends Mage_Core_Controller_Front_Action {        

    public function indexAction()
    {
            $this->loadLayout();
            $this->renderLayout();
    }
    
}