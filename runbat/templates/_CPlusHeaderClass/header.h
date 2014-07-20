<<filename:lowercase:filename>>
#ifndef <<var:uppercase:filename>>_H
#define <<var:uppercase:filename>>_H

#include <string>

class <<var:capitalize:filename>>{
public:
	<<var:capitalize:filename>>();

	virtual ~<<var:capitalize:filename>>();
protected:
private:
	<<var:capitalize:filename>>(const <<var:capitalize:filename>>& other){}
	<<var:capitalize:filename>>& operator=(const <<var:capitalize:filename>>& other){}
};
#endif // <<var:uppercase:filename>>_H