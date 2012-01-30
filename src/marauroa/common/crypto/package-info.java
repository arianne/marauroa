/**
 * Stores Cryptography classes used to handle login process.
 *
 *
 <ul>
 <li>A) If client does not have the RSA public key of the server, then :
 <ol>
 <li>Client ask for the RSA key of the server</li>
 <li>Server send the key to the client</li>
 </ol>

 <p>From now own, I'll call K the key of the server, I'll call K(x), the
 message x encrypted with key K. I'll call K*(y) the message y decrypted
 with key K. Please note that K*(K(x)) = K(K*(x)) = x.</p>

 <p>I'll call H(x) the hash of the value x.</p>

 <p>I'll call x^y the operation x xor y. Please note that 
 x^y^x = x^x^y = y^x^x = y</p>

 <p>I'll call l the client login and p the client password.</p>
</li>
<li>
 B) From now on the client knows the key K.
 <ol>
 <li>Client choose a nonce n1 randomly and send a H(n1) to the server</li>
 <li>Server get H(n1), choose a nonce n2 randomly and send n2 to the
 client.</li>
 <li>Client get n2. It compute h = n1 ^ n2
 It compute proof = K(h^p)
 It sends to the server l, n1, and proof</li>
 <li>The server get the 3 values. It checks that the first value he gets
 is H(n1), otherwise he aborts. From n1 and n2, it computes h.
 It gets p from the DB and now it checks that :
 K*(proof)^h == p
 He can now decide if he must log the client in or not.</li>
 </ol>
</li>
</ul>

<p>The n1 and n2 nonce and all the protocol with H(n1) are used to create
 a randomly number that if agreed between the client and the server but
 that none of the two can choose.</p>
 */
package marauroa.common.crypto;

